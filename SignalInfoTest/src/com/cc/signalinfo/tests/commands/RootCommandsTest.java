package com.cc.signalinfo.tests.commands;

import android.os.Environment;
import android.test.AndroidTestCase;
import com.cc.signalinfo.util.system.commands.JavaCommands;
import com.cc.signalinfo.util.system.commands.RootCommands;
import com.cc.signalinfo.util.system.terminal.RootTerminal;

/**
 * User: admin
 * Date: 8/24/12
 * Time: 2:40 AM
 */
@SuppressWarnings("FeatureEnvy")
public class RootCommandsTest extends AndroidTestCase
{
    private RootCommands cmds;
    private RootTerminal terminal;
    private JavaCommands javaCommands;
    private final        String DEBUG_TAG       = getClass().getSimpleName();
    private static final String SD_CARD         = Environment.getExternalStorageDirectory().getAbsolutePath() + '/';
    private static final String DATA_DIR        = "/data/";
    private static final String SYS_DIR         = "/system/";
    private static final String TEST_NAME       = "testfile";
    private static final String FILE_1          = TEST_NAME + "1.txt";
    private static final String FILE_2          = TEST_NAME + "2.txt";
    private static final String TEST_DIR        = SD_CARD + "test/";
    private static final String SETTINGS_DB     = "settings.db";
    private static final String SETTINGS_DB_LOC = "/data/data/com.android.providers.settings/databases/";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        terminal = new RootTerminal();
        cmds = new RootCommands(terminal);
        javaCommands = new JavaCommands();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testCreateDataDirFile()
    {
        assertTrue(DATA_DIR + " could not be mounted.", cmds.mountData(false));

        assertTrue("Could not create " + FILE_1 + " in " + DATA_DIR, cmds.createFile(DATA_DIR, FILE_1));

        // won't work obviously
        assertTrue("Could not create " + FILE_1 + " in " + DATA_DIR, !javaCommands.createFile(DATA_DIR, FILE_1));

        assertTrue("Could not remove " + FILE_1 + " in " + DATA_DIR, cmds.removeFile(DATA_DIR, FILE_1));
    }

    public void testCreateSystemDirFile()
    {
        assertTrue(SYS_DIR + " could not be mounted.", cmds.mountSystem(false));

        assertTrue("Could not create " + FILE_1 + " in " + SYS_DIR, cmds.createFile(SYS_DIR, FILE_1));

        // won't work obviously
        assertTrue("Could not create " + FILE_1 + " in " + SYS_DIR, !javaCommands.createFile(SYS_DIR, FILE_1));

        assertTrue("Could not remove " + FILE_1 + " in " + SYS_DIR, cmds.removeFile(SYS_DIR, FILE_1));
    }

    public void testGetSettingsDB()
    {
        assertTrue(DATA_DIR + " could not be mounted.", cmds.mountData(false));
        assertTrue(SETTINGS_DB_LOC + ' ' + SETTINGS_DB + " could not be copied to "
            + SD_CARD, cmds.copyFile(SETTINGS_DB_LOC + SETTINGS_DB, SD_CARD + SETTINGS_DB));
    }

}
