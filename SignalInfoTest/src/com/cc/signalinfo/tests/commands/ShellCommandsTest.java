package com.cc.signalinfo.tests.commands;

import android.os.Environment;
import android.test.AndroidTestCase;
import android.util.Log;
import com.cc.signalinfo.util.system.commands.ShellCommands;
import com.cc.signalinfo.util.system.terminal.ShellTerminal;
import com.cc.signalinfo.util.system.terminal.TerminalBase;

import java.io.File;
import java.util.List;
import java.util.Map;


/**
 * User: admin
 * Date: 8/24/12
 * Time: 2:40 AM
 */
@SuppressWarnings("FeatureEnvy")
public class ShellCommandsTest extends AndroidTestCase
{
    private ShellCommands cmds;
    private TerminalBase  terminal;
    private final        String DEBUG_TAG = getClass().getSimpleName();
    private static final String SD_CARD   = Environment.getExternalStorageDirectory().getAbsolutePath() + '/';
    private static final String TEST_NAME = "testfile";
    private static final String FILE_1    = TEST_NAME + "1.txt";
    private static final String FILE_2    = TEST_NAME + "2.txt";
    private static final String TEST_DIR  = SD_CARD + "test/";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        terminal = new ShellTerminal();
        cmds = new ShellCommands(terminal, null);
        String rmFilesCmd = "busybox rm -f " + TEST_DIR + '*';
        cmds.createDir(TEST_DIR);
        assertTrue("Could not remove test files from test folder", cmds.execute(rmFilesCmd));
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Simple test to see if a file is created by running a terminal command.
     * Test creates a file on the sdcard and checks to see if it was created.
     */
    public void testCreateOneFileOnSdCard()
    {
        Log.d(DEBUG_TAG, getName() + ": " + "Starting " + this.getName());
        // The following fails when called with su or sh and should not.
        boolean success = cmds.createFile(SD_CARD, FILE_1);
        Log.d(DEBUG_TAG, getName() + ": " + "Ran created file, returned: " + success);

        for (Map.Entry<String, String> cmd : terminal.getCommandList().entrySet()) {
            assertTrue("command to create " + FILE_1 +
                " did not complete successfully. Output of the command was:\n\n" +
                cmd.getKey() + '\n' +
                terminal.getCommandOutput(cmd.getValue()), success);
            Log.d(DEBUG_TAG, getName() + ": " + "Executed Command: " + cmd.getKey());
            Log.d(DEBUG_TAG, getName() + ": " + "Command Output: " + cmd.getValue());
        }

        Log.d(DEBUG_TAG, "Testing Complete for: " + this.getName());

        assertTrue("Could not remove file from sdcard", cmds.removeFile(SD_CARD, FILE_1));
    }

    /**
     * Creates a bunch of files on the sdcard.
     * Running this takes around .9 to 1.14 seconds
     *
     * Thus, it's best to do things in groups if it's not necessary to check the
     * execution validity after every command.
     */
    public void testCreateManyFilesOnSdCard()
    {
        int filesToCreate = 50;
        String fileCheckCmd = "busybox ls -1 " + TEST_DIR + " | wc -l";
        String rmFilesCmd = "busybox rm -f " + TEST_DIR + '*';
        boolean success;


        double startTime = System.currentTimeMillis() / 1000.0d;

        for (int i = 0; i < filesToCreate; ++i) {
            success = cmds.createFile(TEST_DIR, TEST_NAME + i);
            if (success) {
                Log.d(DEBUG_TAG, TEST_NAME + i + " was created!!!");
            }
            else {
                Log.e(DEBUG_TAG, TEST_NAME + i + " could not be created!! exiting...");
                break;
            }
        }
        double endTime = System.currentTimeMillis() / 1000.0d;
        double totalTime = endTime - startTime;

        Log.d(DEBUG_TAG, "Total time to create " + filesToCreate + " files: " + totalTime + " seconds");


        success = cmds.execute(fileCheckCmd);

        for (Map.Entry<String, String> cmd : terminal.getCommandList().entrySet()) {

            Log.d(DEBUG_TAG, getName() + ": " + "Executed Command: " + cmd.getKey());
            Log.d(DEBUG_TAG, getName() + ": " + "Command Output: " + cmd.getValue());
        }

        assertTrue("Shell command to see if " + filesToCreate + " files were created failed", success);
        assertEquals("Did not create " + filesToCreate + " files"
            , filesToCreate
            , Integer.parseInt(terminal.getCommandOutput(fileCheckCmd)));

        Log.d(DEBUG_TAG, "Removed all starting files from previous tests");
        assertTrue("Could not remove test files from test folder", cmds.execute(rmFilesCmd));
    }

    /**
     * Creates a bunch of files on the sdcard.
     * Running this to create 40 files takes around .29 to .31 seconds
     *
     * Much much quicker than doing each file creation in its own process.
     */
    public void testCreateManyFilesInOneProcessOnSdCard()
    {
        int filesToCreate = 50;
        String createFilesCmd = "busybox touch " + TEST_DIR + TEST_NAME;
        String fileCheckCmd = "busybox ls -1 " + TEST_DIR + " | wc -l";
        String rmFilesCmd = "busybox rm -f " + TEST_DIR + '*';
        boolean success;


        double startTime = System.currentTimeMillis() / 1000.0d;

        String[] createFileCmds = new String[filesToCreate];

        for (int i = 0; i < filesToCreate; ++i) {
            createFileCmds[i] = createFilesCmd + i;
        }
        success = cmds.execute(createFileCmds);

        if (success) {
            Log.d(DEBUG_TAG, getName() + ": " + TEST_NAME + " from 0 to " + filesToCreate + " was created!!!");
        }
        else {
            Log.e(DEBUG_TAG, getName() + ": " + TEST_NAME + " from 0 to " + filesToCreate + " could not be created!! exiting...");
        }

        double endTime = System.currentTimeMillis() / 1000.0d;
        double totalTime = endTime - startTime;

        Log.d(DEBUG_TAG, getName() + ": " + "Total time to create " + filesToCreate + " files: " + totalTime + " seconds");
        success = cmds.execute(fileCheckCmd);

        for (Map.Entry<String, String> cmd : terminal.getCommandList().entrySet()) {
            Log.d(DEBUG_TAG, "Executed Command: " + cmd.getKey());
            Log.d(DEBUG_TAG, "Command Output: " + cmd.getValue());
        }

        assertTrue("Shell command to see if " + filesToCreate + " files were created failed", success);
        assertEquals("Did not create " + filesToCreate + " files"
            , filesToCreate
            , Integer.parseInt(terminal.getCommandOutput(fileCheckCmd)));

        assertTrue("Could not remove test files from test folder", cmds.execute(rmFilesCmd));
        Log.d(DEBUG_TAG, "Removed all starting files from previous tests");
    }

    /**
     * Creates a few files and a directory and checks to see if if the size of
     * the directory is returned properly.
     */
    public void testGetDirectorySizeRecursively()
    {
        String rmFilesCmd = "busybox rm -f " + TEST_DIR + '*';

        assertTrue("Could not create " + TEST_DIR + FILE_1, cmds.execute("echo 'blah' > " + TEST_DIR + FILE_1));
        assertTrue("Could not create " + TEST_DIR + FILE_2, cmds.execute("echo 'blah' > " + TEST_DIR + FILE_2));
        long dirSize = cmds.getDirSizeRecursively(TEST_DIR);
        assertEquals(TEST_DIR + " size was incorrect", 12288, dirSize);
        assertTrue("Could not remove test files from test folder", cmds.execute(rmFilesCmd));

        List<File> apkFiles = cmds.findFiles("", "");
    }
}
