package org.agilityfc;

import com.google.common.base.Charsets;
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
import org.agilityfc.util.CompositeX509TrustManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.time.Duration;
import java.util.Base64;

@Slf4j
@PluginDescriptor(
    name = "Agility FC"
)
public class AgilityFcPlugin extends Plugin
{
    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private Gson gson;

    @Inject
    private AgilityFcConfig config;

    private DonationRemote remote;
    private OkHttpClient httpClient;
    private NavigationButton navButton;

    private static KeyStore keyStoreForCertificate(String cert)
        throws CertificateException, KeyStoreException, IOException,
            NoSuchAlgorithmException
    {
        byte[] b = cert.getBytes(StandardCharsets.UTF_8);
        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        Certificate c = fact.generateCertificate(new ByteArrayInputStream(b));
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        ks.load(null, null);
        ks.setCertificateEntry("cert", c);

        return ks;
    }

    private static Pair<SSLContext, X509TrustManager>
        sslContextForCertificate(String cert)
        throws CertificateException, KeyStoreException, IOException,
            NoSuchAlgorithmException, KeyManagementException
    {
        KeyStore ks = keyStoreForCertificate(cert);
        X509TrustManager tm = CompositeX509TrustManager.getTrustManager(ks);
        SSLContext sc = SSLContext.getInstance("TLS");

        sc.init(null, new X509TrustManager[] {tm}, null);

        return new ImmutablePair<>(sc, tm);
    }

    private DonationRemote parseRemote(String remote)
    {
        byte[] b = Base64.getDecoder().decode(remote);
        String s = new String(b, Charsets.UTF_8);
        DonationRemote r = gson.fromJson(s, DonationRemote.class);

        if (r == null)
        {
            throw new RuntimeException("Empty remote string");
        }
        else if (!StringUtils.isNotEmpty(r.getUrl()))
        {
            throw new RuntimeException("Remote string missing URL");
        }

        return r;
    }

    private static OkHttpClient makeClient(String key, String cert)
        throws CertificateException, KeyStoreException, IOException,
            NoSuchAlgorithmException, KeyManagementException
    {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(10));

        if (StringUtils.isNotEmpty(key))
        {
            builder.addInterceptor(chain ->
                chain.proceed(chain.request().newBuilder()
                    .header("Authorization", Credentials.basic("", key))
                    .build()));
        }

        if (StringUtils.isNotEmpty(cert))
        {
            var p = sslContextForCertificate(cert);
            builder
                .sslSocketFactory(p.getLeft().getSocketFactory(), p.getRight())
                .hostnameVerifier((s, sslSession) -> true);
        }

        return builder.build();
    }

    private void cacheRemote()
    {
        try
        {
            remote = parseRemote(config.remote());
        }
        catch (Exception e)
        {
            log.error("Parsing remote config failed", e);
            remote = null;
            return;
        }

        try
        {
            httpClient = makeClient(config.key(), remote.getCert());
        }
        catch (Exception e)
        {
            log.error("Making HTTP client failed", e);
            remote = null;
            httpClient = null;
        }
    }

    public Call makeCall(DonationInfo di)
    {
        if (httpClient == null)
        {
            throw new RuntimeException("No remote configured");
        }

        Request request = DonationRequest.builder(di)
            .url(remote.getUrl())
            .build();

        return httpClient.newCall(request);
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
        cacheRemote();
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged e)
    {
        cacheRemote();
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
