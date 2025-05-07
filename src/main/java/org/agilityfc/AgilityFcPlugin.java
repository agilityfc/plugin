package org.agilityfc;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
    name = "Agility FC"
)
public class AgilityFcPlugin extends Plugin
{
    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private AgilityFcConfig config;

    private NavigationButton navButton;

    @Provides
    AgilityFcConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(AgilityFcConfig.class);
    }

    @Override
    protected void startUp() throws Exception
    {
        navButton = NavigationButton.builder()
            .tooltip("Agility FC")
            .icon(ImageUtil.loadImageResource(getClass(), "icon.png"))
            .priority(10)
            .panel(injector.getInstance(AgilityFcPanel.class))
            .build();

        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown() throws Exception
    {
        clientToolbar.removeNavigation(navButton);
    }
}
