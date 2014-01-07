package com.cc.signalinfo.util.system.terminal;

import android.util.Log;
import android.util.Pair;

import java.util.Map;

/**
 * Processes shell commands for those that have
 * root. Any special root only commands should go here.
 * Otherwise, they should go in the base if shared.
 */
public class RootTerminal extends TerminalBase
{
    /**
     * Command to be executed to see if we have root or not.
     * If this does not equal 0 on execute, then we do not have root.
     */
    private static final String ROOT_TEST_CMD    = "id\n";
    /**
     * Should be somewhere in the output of executing the test command
     * for root. If it isn't, then we don't have root.
     */
    private static final String ROOT_TEST_RESULT = "uid=0";

    /**
     * Creates a new root process to execute
     * shell commands.
     */
    public RootTerminal()
    {
        this.shellType = "su";
    }

    /**
     * Creates a new root process to execute shell commands.
     *
     * @param checkForRoot - Should we check for root? Best practice
     * would be to check this every time the app is run in case they
     * temp unrooted or something. Don't need to check it every time though.
     *
     * @throws ExceptionInInitializerError - thrown if we do not obtain root.
     * Should catch this and notify the user to enable root.
     */
    public RootTerminal(boolean checkForRoot) throws ExceptionInInitializerError
    {
        this();

//        if (!hasRoot()) {
//            throw new ExceptionInInitializerError("Could not obtain root, sorry");
//        }
    }

    /**
     * Determines if we have root or not on the device. Most likely
     * should be checked every time the app is run to ensure the user
     * did not temp unroot and forgot to enabling later.
     *
     * @param callback - where to send the results of this command
     */
    public final void hasRoot(CommandCallback callback)
    {
        // execute command (get the result in the callback and pass to hasRootResult())
        executeAsyncCmds(callback, ROOT_TEST_CMD);
    }

    /**
     * Just a helper to deal with the async result so the activity or whatever does
     * not have to do that itself. Pass the result of the async callback pair here instead.
     *
     * @param commandResult - result from the async command execution
     * @return true if we have root
     */
    public final boolean hasRootResult(Pair<Boolean, Map<String, String>> commandResult)
    {
        // put the output in a string
        String logger = commandResult.second.get(ROOT_TEST_CMD);
        // don't fail if return is null
        if (logger == null) {
            Log.d(DEBUG_TAG, "Root check failed; return was null bailing...");
            return false;
        }
        // debugging
        Log.i(DEBUG_TAG, "returned from root check: " + logger);
        // check if we have uid=0
        if (logger.contains(ROOT_TEST_RESULT)) {
            Log.d(DEBUG_TAG, "We have root, yay");
            return true;
        }
        Log.d(DEBUG_TAG, "Device could not obtain root, sorry.");
        return false;
    }
}
