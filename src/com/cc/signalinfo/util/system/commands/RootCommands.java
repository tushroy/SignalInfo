package com.cc.signalinfo.util.system.commands;

import android.util.Log;
import com.cc.signalinfo.util.system.terminal.TerminalBase;

import static com.cc.signalinfo.util.system.terminal.TerminalBase.*;

/**
 * Used to execute commands that need root. Use ShellCommands if root
 * is not required. Note that anything protected or public in ShellCommands
 * may be used here as well (due to inheritance).
 */
public class RootCommands extends ShellCommands
{
    /**
     * Since data can only be accessed with root, add it to this class only.
     */
    private static final String DATA_PARTITION = "/data";

    /**
     * Create a new instance to execute commands that need root.
     *
     * @param shell - root shell to execute the commands with.
     */
    public RootCommands(TerminalBase shell)
    {
        super(shell, null);
        this.shell = shell;
    }

    /**
     * Create a new instance to execute commands that need root.
     *
     * @param shell - root shell to execute the commands with.
     */
    public RootCommands(TerminalBase shell, CommandCallback callback)
    {
        super(shell, callback);
        this.shell = shell;
    }

    /**
     * Mount system in read/write or read only
     *
     * @param readOnly - should we mount partition in read only mode?
     * @return true if mounted executed okay.
     */
    public boolean mountSystem(boolean readOnly)
    {
        Log.d(DEBUG_TAG, "Trying to mount /system");
        if (readOnly) {
            Log.d(DEBUG_TAG, "Mounting system in ro");
            return mountPartition(SYSTEM_PARTITION, "ro");
        }
        else {
            Log.d(DEBUG_TAG, "Mounting system in rw");
            return mountPartition(SYSTEM_PARTITION, "rw");
        }
    }

    /**
     * Mount data in read/write or read only
     *
     * @param readOnly - should we mount partition in read only mode?
     * @return true if mount executed okay.
     */
    public boolean mountData(boolean readOnly)
    {
        Log.d(DEBUG_TAG, "Trying to mount /data");
        if (readOnly) {
            Log.d(DEBUG_TAG, "Mounting data in ro");
            return mountPartition(DATA_PARTITION, "ro");
        }
        else {
            Log.d(DEBUG_TAG, "Mounting data in rw");
            return mountPartition(DATA_PARTITION, "rw");
        }
    }

    /**
     * Helper function to mount a partition.
     *
     * @param directory - directory (partition) to mount
     * @param mountStatus - should be either "rw" or "ro"
     * @return true if mount executed okay.
     */
    private boolean mountPartition(String directory, String mountStatus)
    {
        return shell.executeCmds("mount -o remount " + mountStatus + ' ' + directory);
    }
}