package org.agilityfc;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.util.UUID;

@ConfigGroup("agilityfc")
public interface AgilityFcConfig extends Config
{
    @ConfigSection(
        name = "Donation Service",
        description = "For AFC moderators",
        position = 0)
    String donationSection = "Donation Service";

    @ConfigItem(
        keyName = "url",
        name = "URL",
        description = "The URL of the service to send donation data to",
        secret = true,
        section = donationSection,
        position = 0
    )
    default String url()
    {
        return "";
    }

    @ConfigItem(
        keyName = "key",
        name = "Key",
        description = "The key to use to authenticate with the service",
        secret = true,
        section = donationSection,
        position = 1
    )
    default String key()
    {
        return UUID.randomUUID().toString();
    }
}
