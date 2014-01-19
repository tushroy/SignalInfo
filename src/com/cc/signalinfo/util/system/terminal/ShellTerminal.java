package com.cc.signalinfo.util.system.terminal;

/**
 * User: admin
 * Date: 8/22/12
 * Time: 5:57 AM
 */
public class ShellTerminal extends TerminalBase
{
    /**
     * Create a non-root shell to process terminal commands
     *
     * @param shellType - sh/bash/ash/etc
     */
    public ShellTerminal(String shellType)
    {
        this.shellType = shellType;
    }

    /**
     * Create a non-root shell to process terminal commands
     */
    public ShellTerminal()
    {

    }
}
