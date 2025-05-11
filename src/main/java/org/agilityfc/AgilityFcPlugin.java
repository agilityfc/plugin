package org.agilityfc;

import com.google.gson.Gson;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.time.Duration;

@Slf4j
@PluginDescriptor(
    name = "Agility FC"
)
public class AgilityFcPlugin extends Plugin
{
    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private OkHttpClient httpClient;

    @Inject
    private AgilityFcConfig config;

    private OkHttpClient myHttpClient;
    private NavigationButton navButton;

    private OkHttpClient makeClient(String key)
    {
        OkHttpClient.Builder builder = httpClient.newBuilder()
            .callTimeout(Duration.ofSeconds(10));

        if (StringUtils.isNotEmpty(key))
        {
            builder.addInterceptor(chain ->
                chain.proceed(chain.request().newBuilder()
                    .header("Authorization", Credentials.basic("", key))
                    .build()));
        }

        return builder.build();
    }

    public Call makeCall(DonationInfo di)
    {
        Request request = DonationRequest.builder(di)
            .url(config.url())
            .build();

        return myHttpClient.newCall(request);
    }

    @Override
    protected void startUp() throws Exception
    {
        myHttpClient = makeClient(config.key());
        navButton = NavigationButton.builder()
            .tooltip("Agility FC")
            .icon(ImageUtil.loadImageResource(getClass(), "icon.png"))
            .priority(10)
            .panel(injector.getInstance(AgilityFcPanel.class))
            .build();

        clientToolbar.addNavigation(navButton);
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged e)
    {
        myHttpClient = makeClient(config.key());
    }

    @Override
    protected void shutDown() throws Exception
    {
        clientToolbar.removeNavigation(navButton);
    }

    @Provides
    AgilityFcConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(AgilityFcConfig.class);
    }
}
