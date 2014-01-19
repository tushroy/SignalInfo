package com.cc.signalinfo.util.system.commands;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.cc.signalinfo.util.system.terminal.ShellTerminal;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

/**
 * Class should execute system wide commands using the Java and Android
 * SDKs versus doing it with terminal commands.
 */
public class JavaCommands implements Commands
{
    private final String DEBUG_TAG = getClass().getSimpleName();

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
        try {
            FileUtils.moveFile(new File(fileNamePath), new File(newLocation));
        }
        catch (IOException e) {
            Log.e(DEBUG_TAG, "Could not move file from "
                + fileNamePath + " to " + newLocation, e);
            return false;
        }
        return true;
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
        try {
            FileUtils.copyFile(new File(fileNamePath), new File(newLocation));
        }
        catch (IOException e) {
            Log.e(DEBUG_TAG, "Could not copy file from "
                + fileNamePath + " to " + newLocation, e);
            return false;
        }
        return true;
    }

    /**
     * XXX not really sure we should do it this way
     * Change permissions on a file or directory
     *
     * @param perms - numeric for perms (0777 or whatever)
     * @param path - path to the directory or file (including filename)
     * @return true if permissions changed
     */
    @Override
    public boolean changePerms(String perms, String path)
    {
        File fileToChange = new File(path);

        // if file doesn't exist fail
        if (!fileToChange.exists()) {
            return false;
        }

        // we need each permission but we really just generalize
        // as we only have setReadable() setWritable() setExecutable()
        // contains permissions in this order { Directory*if supplied*; Owner; Group; Other; }
        char[] suppliedPerms = perms.toCharArray();

        // we must account for xxx permissions and xxxx permissions
        char ownerPerm = suppliedPerms[suppliedPerms.length - 2];
        char groupPerm = suppliedPerms[suppliedPerms.length - 1];
        char otherPerm = suppliedPerms[suppliedPerms.length];

        // we base our main permissions off owner permissions
        int intOwnerPerm = Integer.parseInt(String.valueOf(ownerPerm));

        // used to discover if we only want permission grant to apply to Owner
        int intGroupPerm = Integer.parseInt(String.valueOf(groupPerm));
        int intOtherPerm = Integer.parseInt(String.valueOf(otherPerm));

        // we only send off Group permission and Other permission
        int[] intSuppliedPerms = {
            intGroupPerm,
            intOtherPerm
        };

        boolean permsChanged = false;

        switch (intOwnerPerm) {
            case 0:
                permsChanged = setPermissions(fileToChange, intSuppliedPerms, false, false, false);
                break;
            case 1:
                permsChanged = setPermissions(fileToChange, intSuppliedPerms, false, false, true);
                break;
            case 2:
                permsChanged = setPermissions(fileToChange, intSuppliedPerms, false, true, false);
                break;
            case 3:
                permsChanged = setPermissions(fileToChange, intSuppliedPerms, false, true, true);
                break;
            case 4:
                permsChanged = setPermissions(fileToChange, intSuppliedPerms, true, false, false);
                break;
            case 5:
                permsChanged = setPermissions(fileToChange, intSuppliedPerms, true, false, true);
                break;
            case 6:
                permsChanged = setPermissions(fileToChange, intSuppliedPerms, true, true, false);
                break;
            case 7:
                permsChanged = setPermissions(fileToChange, intSuppliedPerms, true, true, true);
                break;
        }
        return permsChanged;
    }

    /**
     * applies permissions as accurately as possible, till android decides to move to java7 :-/
     *
     * @param file file to change permissions
     * @param perms int[0] contains Group permission int[1] contains Other permission
     * @param canRead can read permission for file
     * @param canWrite can write permission for file
     * @param canExecute can execute permission for file
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static boolean setPermissions(File file, int[] perms,
                                          boolean canRead, boolean canWrite, boolean canExecute)
    {
        // by default we use strict permissions
        boolean justOwnerReadable = true;
        boolean justOwnerWritable = true;
        boolean justOwnerExecutable = true;

        // adjust permissions as best java6 will allow
        // see if any other groups are allowed readable permission
        if (/* check Group permission */
            perms[0] == 4 || perms[0] == 5 || perms[0] == 6 || perms[0] == 7
            /* check Other permission */
                || perms[1] == 4 || perms[1] == 5 || perms[1] == 6 || perms[1] == 7) {
            justOwnerReadable = false;
        }

        // see if any other groups are allowed writable permission
        if (/* check Group permission */
            perms[0] == 2 || perms[0] == 3 || perms[0] == 6 || perms[0] == 7
            /* check Other permission */
                || perms[1] == 2 || perms[1] == 3 || perms[1] == 6 || perms[1] == 7) {
            justOwnerWritable = false;
        }

        // see if any other groups are allowed executable permission
        if (/* check Group permission */
            perms[0] == 1 || perms[0] == 3 || perms[0] == 5 || perms[0] == 7
            /* check Other permission */
                || perms[1] == 1 || perms[1] == 3 || perms[1] == 5 || perms[1] == 7) {
            justOwnerExecutable = false;
        }

        // now that we have all the possible information apply our settings
        return file.setReadable(canRead, justOwnerReadable) &&
            file.setWritable(canWrite, justOwnerWritable) &&
            file.setExecutable(canExecute, justOwnerExecutable);
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
        File newFile = new File(newFilePath, file);
        try {
            return newFile.createNewFile();
        }
        catch (IOException e) {
            Log.e(DEBUG_TAG, "Could not create file.", e);
        }
        return false;
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
        File dir = new File(newFilePath);
        try {
            FileUtils.forceMkdir(dir);
        }
        catch (IOException e) {
            Log.e(DEBUG_TAG, "Could not create directory.", e);
        }
        return dir.isDirectory();
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
        return false;
    }


    /**
     * Find out how large a given file is.
     *
     * @param targetFile path to file we want in size
     * @param si units true for SI units false for BINARY
     * @return size of file in appropriate units with unit tag
     *         { "B", "KB", "MB", "GB", "TB" }
     */
    @Override
    public String readableFileSize(String targetFile, boolean si)
    {
        long bytes = new File(targetFile).length();
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        // return the value to 3 decimal places
        return String.format("%.3f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Find a needle...er I mean files in a haystack (directories)
     *
     * @param haystack - where to start the search
     * @param needle - what to search with (partial name of whatever)
     * @return a list of files found in the haystack or null if none found
     */
    @Override
    public List<File> findFiles(String haystack, final String needle)
    {
        IOFileFilter ioFileFilter = new IOFileFilter()
        {
            @Override
            public boolean accept(File file)
            {
                return file.getName().toLowerCase().contains(needle);
            }

            @Override
            public boolean accept(File dir, String name)
            {
                return name.toLowerCase().contains(needle);
            }
        };
        return (List<File>) FileUtils.listFiles(new File(haystack), ioFileFilter, ioFileFilter);
    }

    @Override
    public void launchActivity(String packageName, String activityName)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        ComponentName showSettings = new ComponentName(
            packageName, String.format("%s.%s", packageName, activityName));
    }

    public boolean hasStorage(boolean requireWriteAccess) {
        //TODO: After fix the bug,  add "if (VERBOSE)" before logging errors.
        String state = Environment.getExternalStorageState();
        Log.v(DEBUG_TAG, "storage state is " + state);

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (requireWriteAccess) {
                boolean writable = checkFsWritable();
                Log.v(DEBUG_TAG, "storage writable is " + writable);
                return writable;
            }
            return true;
        }
        return !requireWriteAccess && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    private static boolean checkFsWritable() {
        // Create a temporary file to see whether a volume is really writeable.
        // It's important not to put it in the root directory which may have a
        // limit on the number of files.
        String directoryName = Environment.getExternalStorageDirectory().toString() + "/DCIM";
        File directory = new File(directoryName);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                return false;
            }
        }
        return directory.canWrite();
    }


}