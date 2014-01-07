/**
 * AdBlocker
 * Author: Wes Lanning
 * Copyright 2012
 */
package com.cc.signalinfo.util.system.commands;



import com.cc.signalinfo.util.SignalHelpers;
import com.cc.signalinfo.util.system.terminal.RootTerminal;
import com.cc.signalinfo.util.system.terminal.TerminalBase;

import java.io.File;
import java.util.List;

/**
 * Class should execute system wide commands using terminal (shell) commands
 * instead of doing it with the Java and Android SDKs.
 *
 * All commands listed here should be be able to be run without root under
 * some circumstance. Anything that should always require root should go in
 * RootCommands instead.
 */
public class ShellCommands implements Commands
{
    protected final        String DEBUG_TAG        = getClass().getSimpleName();
    protected static final String SYSTEM_PARTITION = "/system";
    protected TerminalBase shell;
    protected TerminalBase.CommandCallback callback = null;

    // TODO: make commands able to be executed in a group transaction. If possible, do it transparently (or try, idk yet)

    /**
     * Create a new instance to execute commands that are shared by
     * root and non root terminal instances.
     *
     * @param shell - shell to execute commands with (root or non root)
     * @param callback
     */
    public ShellCommands(TerminalBase shell, TerminalBase.CommandCallback callback)
    {
        this.shell = shell;
        this.callback = callback;
    }

    /**
     * Moves a file to a new location
     *
     * @param fileNamePath - full path + filename
     * @param newLocation - where to move the file + new file name
     * @return true if file was moved ok
     */
    @Override
    public boolean moveFile(String fileNamePath, String newLocation)
    {
        return shell.executeCmds("mv " + fileNamePath + ' ' + newLocation);
    }

    /**
     * Copies a file to a new location
     *
     * @param fileNamePath - full path + filename
     * @param newLocation - where to copy the file + new file name
     * @return true if file was copied
     */
    @Override
    public boolean copyFile(String fileNamePath, String newLocation)
    {
        return shell.executeCmds("cp " + fileNamePath + ' ' + newLocation);
    }

    /**
     * Change permissions on a file or directory
     *
     * @param perms - numeric for perms (0777 or whatever)
     * @param path - path to the directory or file (including filename)
     * @return true if permissions changed
     */
    @Override
    public boolean changePerms(String perms, String path)
    {
        return shell.executeCmds("chmod " + perms + ' ' + path);
    }

    /**
     * Creates an empty file at some location.
     *
     * @param newFilePath - location for the file
     * @param file - name of the new file
     * @return true if created
     */
    @Override
    public boolean createFile(String newFilePath, String file)
    {
        return shell.executeCmds("busybox touch " + newFilePath + file);
    }

    /**
     * Creates an empty directory at some location.
     *
     * @param newFilePath - location for the directory
     * @return true if created
     */
    @Override
    public boolean createDir(String newFilePath)
    {
        return shell.executeCmds("mkdir -p " + newFilePath);
    }

    /**
     * Removes a file from some location
     *
     * @param fileNamePath - path to the file
     * @param file - name of the file to remove
     * @return true if file was removed okay
     */
    @Override
    public boolean removeFile(String fileNamePath, String file)
    {
        return shell.executeCmds("busybox rm -f " + fileNamePath + file);
    }

    /**
     * Find out how large a given file is.
     *
     * @param targetFile path to file under question
     * @param si units true for SI units false for BINARY
     * @return size of file in appropriate units with unit tag
     *         { "B", "KB", "MB", "GB", "TB" }
     */
    @Override
    public String readableFileSize(String targetFile, boolean si)
    {
        // ls -1 -sh filename | cut -d\  -f1 or something instead
        long targetSize = Long.parseLong(shell.getCommandOutput("ls -1 -sh " + targetFile + " | cut -d\\ -f1"));
        int unit = si ? 1000 : 1024;
        if (targetSize < unit) {
            return targetSize + " B";
        }
        int exp = (int) (Math.log(targetSize) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        // return the value to 3 decimal places
        return String.format("%.3f %sB", targetSize / Math.pow(unit, exp), pre);
    }

    /**
     * Find a needle...er I mean files in a haystack (directories)
     *
     * @param haystack - where to start the search
     * @param needle - what to search with (partial name of whatever)
     * @return a list of files found in the haystack or null if none found
     */
    @Override
    public List<File> findFiles(String haystack, String needle)
    {
        return null;
    }

    @Override
    public void launchActivity(String packageName, String activityName)
    {
        shell.executeAsyncCmds(callback, String.format("am start -a %s -n %s/%s.%s",
            "android.intent.action.MAIN",
            packageName,
            packageName,
            activityName));
    }

    /**
     * gets the total size of the directory in bytes
     *
     * @param dir to be analyzed
     * @return size of the directory in bytes
     */
    public long getDirSizeRecursively(String dir) {
        if (shell.executeCmds("busybox du -k -c " + dir + " | busybox grep total | busybox cut -d\\t -f1")) {
            return Long.parseLong(shell.getCommandOutput("busybox du -k -c " + dir + " | busybox grep total | busybox cut -d\\t -f1").trim()) * 1024;
        }
        return -1;
    }

    /**
     * Nondescript command to execute in the terminal
     * shell. If the command is repetitive, don't use this.
     * Instead, create a function to do it there.
     *
     * This is here for simplicity in order to avoid "one off"
     * functions being created where they will never really be used.
     *
     * <b>DO NOT ABUSE THIS FUNCTION!!!!!!!!!!</b>
     *
     * @param cmd - command to run
     * @return true if the command executed okay
     */
    public boolean execute(String... cmd)
    {

        return shell.executeCmds(cmd);
    }
}
