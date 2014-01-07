package com.cc.signalinfo.util.system.commands;


import java.io.File;
import java.util.List;

/**
 * Why have an interface for commands? So we can transparently switch
 * between using Java commands for things and terminal commands where they
 * both have overlap.
 *
 * Might want to use Java ones for performance in some cases.
 * Other times, might want to use terminal for root, etc.
 *
 * This should make the code elsewhere much much cleaner when used.
 */
public interface Commands
{
    /**
     * Moves a file to a new location
     *
     * @param fileNamePath - full path + filename
     * @param newLocation - where to move the file + new file name
     * @return true if file was moved ok
     */
    boolean moveFile(String fileNamePath, String newLocation);

    /**
     * Copies a file to a new location
     *
     * @param fileNamePath - full path + filename
     * @param newLocation - where to copy the file + new file name
     * @return true if file was copied
     */
    boolean copyFile(String fileNamePath, String newLocation);

    /**
     * Change permissions on a file or directory
     *
     * @param perms - numeric for perms (0777 or whatever)
     * @param path - path to the directory or file (including filename)
     * @return true if permissions changed
     */
    boolean changePerms(String perms, String path);

    /**
     * Creates an empty file at some location.
     *
     * @param newFilePath - location for the file
     * @param file - name of the new file
     * @return true if created
     */
    boolean createFile(String newFilePath, String file);

    /**
     * Creates an empty directory at some location.
     *
     * @param newFilePath - location for the directory
     * @return true if created
     */
    boolean createDir(String newFilePath);

    /**
     * Removes a file from some location
     *
     * @param fileNamePath - path to the file
     * @param file - name of the file to remove
     * @return true if file was removed okay
     */
    boolean removeFile(String fileNamePath, String file);

    /**
     * Find out how large a given file is.
     *
     * @param targetFile path to file we want size of
     * @param si units true for SI units false for BINARY
     * @return size of file in appropriate units with unit tag
     *         { "B", "KB", "MB", "GB", "TB" }
     */
    String readableFileSize(String targetFile, boolean si);

    /**
     * Find a needle...er I mean files in a haystack (directories)
     *
     * @param haystack - where to start the search
     * @param needle - what to search with (partial name of whatever)
     * @return a list of files found in the haystack or null if none found
     */
    List<File> findFiles(String haystack, String needle);

    /**
     * Launch an activity based on the package and activity name
     *
     * @param packageName - in the format com.example.packages
     * @param activityName - in the format ClassActivityName
     */
    void launchActivity(String packageName, String activityName);
}
