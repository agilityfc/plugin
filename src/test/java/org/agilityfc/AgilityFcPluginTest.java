package org.agilityfc;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AgilityFcPluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(AgilityFcPlugin.class);
        RuneLite.main(args);
    }
}
