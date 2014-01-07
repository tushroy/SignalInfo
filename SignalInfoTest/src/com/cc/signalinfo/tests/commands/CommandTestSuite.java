package com.cc.signalinfo.tests.commands;

import android.test.suitebuilder.TestSuiteBuilder;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * User: admin
 * Date: 8/24/12
 * Time: 3:13 AM
 */
public class CommandTestSuite extends TestSuite
{
    public static Test suite()
    {
        return new TestSuiteBuilder(CommandTestSuite.class)
            .includeAllPackagesUnderHere()
            .build();
    }
}
