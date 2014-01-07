package com.cc.signalinfo.tests;

import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import com.cc.signalinfo.activities.MainActivity;
import com.cc.signalinfo.util.system.terminal.RootTerminal;
import com.cc.signalinfo.util.system.terminal.TerminalBase;

import java.io.File;


public class TerminalTest extends ActivityInstrumentationTestCase2<MainActivity>
{
    TerminalBase suShell;
    private static final String FILE_1 = Environment.getExternalStorageState() + "/testfile1.txt";
    private static final String FILE_2 = Environment.getExternalStorageState() + "/testfile2.txt";

    public void setUp() throws Exception
    {
        super.setUp();
        suShell = new RootTerminal();
    }

    public void testSuOrSH() throws Exception
    {

    }

    public void testCanSU() throws Exception
    {

    }

    /**
     * Simple test to see if a file is created by running a terminal command.
     * Test creates a file on the sdcard and checks to see if it was created.
     */
    public void testCreateFileOnSdCard()
    {
        String touchCommand = "busybox touch " + FILE_1;
        // The following fails when called with su or sh and should not.
        boolean success = suShell.executeCmds(touchCommand);
        assertTrue("command to create " + FILE_1 + " did not complete successfully", success);

        File testFile = new File(FILE_1);
        assertTrue(FILE_1 + " was not created", testFile.exists() && !testFile.isDirectory());
    }
/*
    public void testCopySdCardFiles()
    {
        FragmentBootAnims bootFrag = new FragmentBootAnims();
        String copyCommand = String.format("cp /system/media/bootanimation.zip %s",
            Environment.getExternalStorageDirectory() + Vars.BOOTANIM_STORAGE_FOLDER + "/" + bootFrag.getBestName() + ".zip");

        boolean success = suShell.executeCmds(copyCommand);
    }*/

    /**
     * Initialize the tests. Required by Android.
     */
    public TerminalTest()
    {
        super(MainActivity.class);
    }
}
