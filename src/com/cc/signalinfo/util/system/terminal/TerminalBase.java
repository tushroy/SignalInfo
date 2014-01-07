/**
 * AdBlocker
 * Author: Wes Lanning
 * Copyright 2012
 */
package com.cc.signalinfo.util.system.terminal;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Contains the core methods needed to run terminal commands from an android app
 * Do not run this class directly. Instead, extend it to add commands for use.
 */
public class TerminalBase
{
    /**
     * The process that shell commands will run in.
     */
    // private Process process;

    /**
     * Writes the commands to the shell process for execution. Please close
     * this manually and don't rely on the class to do it. That's bad and I
     * will hate you for it >:O
     */
    // protected DataOutputStream os;

    /**
     * Tag for logging/debugging in logcat
     */
    protected static final String DEBUG_TAG = TerminalBase.class.getSimpleName();
    /**
     * Used to store the output for each command executed.
     * Key will be the command and the value will be the output.
     *
     * This way we can see if the commands run okay or not or display
     * info to the user.
     */
    protected final Map<String, String> commandOutput;
    /**
     * First command run to create the shell process
     * should we be running as root or not.
     *
     * either su or sh/bash/ash/etc
     */
    protected       String              shellType;

    /**
     * Creates a new process to execute shell commands.
     *
     * Should not really be used directly. Instead, extend this class
     * and use via the child class.
     */
    protected TerminalBase()
    {
        commandOutput = new LinkedHashMap<String, String>(10);
        // default to plain old shell
        shellType = "sh";
    }

    /**
     * Runs a terminal command on the device asynchronously without wrapping.
     *
     * Does not save the command output. Use executeCmds if saving output is required.
     *
     * @param callback - object to notify of the execution result (true if all commands completed okay)
     * @param cmds - Commands to run
     */
    public void executeAsyncCmds(CommandCallback callback, String... cmds)
    {
        new AsyncTerminal().execute(callback, cmds);
    }

    /**
     * Runs a terminal command on the device.
     *
     * This will block the main thread unless wrapped in an AsyncTask or similar.
     *
     * @param outputDump - should the results be dumped afterwards for debugging?
     * @param cmds - Commands to run
     * @return true if all commands completed successfully
     */
    public Map<String, String> executeCmds(boolean outputDump, String... cmds)
    {
        DataOutputStream os = null;
        BufferedReader br = null;
        Process process = null;
        boolean success = false;
        Map<String, String> commandOutput = new LinkedHashMap<>(10);

        try {
            // open the parent process to execute the commands
            process = Runtime.getRuntime().exec(shellType);
            os = new DataOutputStream(process.getOutputStream());
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // loop and run all the commands given,
            // store each in a queue for later output if needed

            for (String singleCmd : cmds) {
                os.writeBytes(singleCmd + ";\n");
                os.flush();

                // store the output of each command in the map
                // note that it won't dump anything here if we only have one command to execute.

                if (br.ready()) {
                    commandOutput.put(singleCmd, br.readLine());
                }
                else {
                    commandOutput.put(singleCmd, "");
                }
            }
            os.writeBytes("exit\n");
            os.flush();

            if (cmds.length == 1) {
                Log.d(DEBUG_TAG, "One command executed");
                // buffer never dumps anything in loop if only one command runs so we need to get it here
                commandOutput.put(cmds[0], br.readLine());
            }

            commandOutput.put("exit\n", br.readLine());
            Log.d(DEBUG_TAG, "Waiting for process to finish");
            process.waitFor();
            Log.d(DEBUG_TAG, "Process finished.");

            if (process.exitValue() == 0) {
                success = true;
            }
        } catch (IOException e) {
            // dump out the commands that did run for debugging.
            if (outputDump) {
                commandOutputDump("Could not run terminal commands\n", e);
            }
        } catch (InterruptedException e) {
            if (outputDump) {
                commandOutputDump("Could not create a shell process and/or gain root\n", e);
            }
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                if (outputDump) {
                    commandOutputDump("Could not close process input/output\n", e);
                }
            }
        }
        return commandOutput;
    }

    /**
     * Runs a terminal command on the device.
     *
     * This will block the main thread unless wrapped in an AsyncTask or similar.
     *
     * @param outputResults - should the results be stored for output or debugging?
     * @param cmds - Commands to run
     * @return true if all commands completed successfully
     */
    public boolean executeCmds(String... cmds)
    {
        DataOutputStream os = null;
        BufferedReader br = null;
        Process process = null;
        boolean success = false;

        try {
            // open the parent process to execute the commands
            process = Runtime.getRuntime().exec(shellType);
            os = new DataOutputStream(process.getOutputStream());
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // loop and run all the commands given,
            // store each in a queue for later output if needed

            for (String singleCmd : cmds) {
                os.writeBytes(singleCmd + ";\n");
                os.flush();

                // store the output of each command in the map
                // note that it won't dump anything here if we only have one command to execute.

            }
            os.writeBytes("exit\n");
            os.flush();

            if (cmds.length == 1) {
                Log.d(DEBUG_TAG, "One command executed");
                // buffer never dumps anything in loop if only one command runs so we need to get it here

            }
            Log.d(DEBUG_TAG, "Waiting for process to finish");
            process.waitFor();
            Log.d(DEBUG_TAG, "Process finished.");

            if (process.exitValue() == 0) {
                success = true;
            }
        } catch (IOException | InterruptedException ignored) {

        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException ignored) {

            }
        }
        return success;
    }

    /**
     * What type of shell are we executing the current commands with?
     *
     * @return either su or sh/bash/ash/etc
     */
    public String getShellType()
    {
        return shellType;
    }

    /**
     * Returns the result of a command that was executed.
     *
     * @param command - name of the command that was previously executed
     * @return the output of the command (if any)
     */
    public String getCommandOutput(String command)
    {
        return commandOutput.get(command);
    }

    /**
     * Returns a read only Map containing all the commands
     * executed and their output (if any output was shown)
     *
     * @return the set of commands executed
     */
    public Map<String, String> getCommandList()
    {
        return Collections.unmodifiableMap(commandOutput);
    }

    /**
     * Clears the output log for previous commands
     * that were executed.
     */
    public void clearCommandLog()
    {
        commandOutput.clear();
    }

    /**
     * Used for logging purposes. Dumps out commands that were processed
     * so far and the exception that caused things not to complete.
     *
     * @param logReason - reason for failure
     * @param e - the exception that caused the failure
     */
    private void commandOutputDump(String logReason, Exception e)
    {
        StringBuilder executedCmds = new StringBuilder(logReason);
        int commandNum = 1;

        for (Map.Entry<String, String> entry : commandOutput.entrySet()) {
            executedCmds.append("Executed command #")
                .append(commandNum)
                .append(": ")
                .append(entry.getKey())
                .append(", Command output: ")
                .append(entry.getValue())
                .append('\n');
        }
        Log.e(DEBUG_TAG, executedCmds.toString(), e);
    }

    /**
     * Notifies an activity or class of the result of executing some commands
     */
    public interface CommandCallback
    {
        /**
         * Notifies an activity or class of the result of
         * executing some commands.
         *
         * @param executeResult - result of executing some commands (true if they all ran okay)
         */
        void notifyCmdResult(Pair<Boolean, Map<String, String>> executeResult);
    }

    private class AsyncTerminal extends AsyncTask<Object, Void, Object[]>
    {
        @Override
        protected Object[] doInBackground(Object... params)
        {
            CommandCallback callback = (CommandCallback) params[0];
            String[] cmds = (String[]) params[1];

            boolean success = executeCmds(cmds);
            return new Object[]{success, callback};
        }

        @Override
        protected void onPostExecute(Object... result)
        {
            boolean success = (boolean) result[0];
            CommandCallback callback = (CommandCallback) result[1];
            // because I can't pass null directly and don't want to create an object
            Map<String, String> noResults = new HashMap<>(0);
            callback.notifyCmdResult(Pair.create(success, noResults));
        }
    }

    private class AsyncTerminalOutput extends AsyncTask<Object, Void, Object[]>
    {
        @Override
        protected Object[] doInBackground(Object... params)
        {
            CommandCallback callback = (CommandCallback) params[0];
            String[] cmds = (String[]) params[1];
            Map<String, String> results = executeCmds(false, cmds);
            return new Object[]{results, callback};
        }

        @Override
        protected void onPostExecute(Object... result)
        {
            Map<String, String> results = (Map<String, String>) result[0];
            CommandCallback callback = (CommandCallback) result[1];
            callback.notifyCmdResult(Pair.create(true, results));
        }
    }
/*
    Currently this isn't used because I don't feel child classes
    need an abstract method for it.

    /**
     * First command run to create the shell process
     * should we be running as root or not.
     *
     * @return either su or sh/bash/ash/etc
     *//*
    protected abstract String getShellType();*/


/*    Not needed right now if process and such is not class
    wide scope. Would probably not be the best way to do things
    anyways.*/

    /**
     * This should not be run really. Manually close the shell
     * instead. If you don't, I hate you (in the nicest possible way).
     *
     * @throws Throwable
     */
/*    @Override
    protected void finalize() throws Throwable
    {
        Log.wtf(DEBUG_TAG, "HEY! YOU WERE SUPPOSED TO CLOSE THE SHELL STREAM DAMNIT!!!");
        try {
            if (os != null) {
                this.os.close();
                os = null;
            }
        }
        catch (IOException e) {
            Log.wtf(DEBUG_TAG, "HEY! YOU WERE SUPPOSED TO CLOSE THE SHELL STREAM DAMNIT" +
                " AND NOW YOU ALSO CAUSED AN EXCEPTION >:O");
        } finally {
            super.finalize();
        }
    }*/

    /**
     * Closes the terminal shell. Closes the terminal connection. MAKE SURE YOU DO THIS!!!
     */
/*    public void close() throws IOException
    {
        if (os != null) {
            os.close();
            os = null;
        }

        if (process != null) {
            process.destroy();
            process = null;
        }
    }*/
}
