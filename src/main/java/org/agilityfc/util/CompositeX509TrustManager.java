package org.agilityfc.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

// NOTE: Adapted from
// <https://gist.github.com/HughJeffner/6eac419b18c6001aeadb>.

/**
 * Represents an ordered list of {@link X509TrustManager}s with additive trust.
 * If any one of the composed managers trusts a certificate chain, then it is
 * trusted by the composite manager.
 *
 * This is necessary because of the fine-print on {@link SSLContext#init}: Only
 * the first instance of a particular key and/or trust manager implementation
 * type in the array is used. For example, only the first {@link X509KeyManager}
 * in the array will be used.
 *
 * @author codyaray
 */
public class CompositeX509TrustManager implements X509TrustManager
{
    private final List<X509TrustManager> trustManagers;

    public CompositeX509TrustManager(List<X509TrustManager> trustManagers)
    {
        this.trustManagers = ImmutableList.copyOf(trustManagers);
    }

    public CompositeX509TrustManager(KeyStore keystore)
        throws NoSuchAlgorithmException, KeyStoreException
    {
        this.trustManagers = ImmutableList.of(
            getDefaultTrustManager(), getTrustManager(keystore));
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException
    {
        for (X509TrustManager trustManager : trustManagers)
        {
            try
            {
                trustManager.checkClientTrusted(chain, authType);
                return;
            }
            catch (CertificateException e)
            {
                // Continue checking other trust managers.
            }
        }

        throw new CertificateException("None of the TrustManagers trust this certificate chain");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException
    {
        for (X509TrustManager trustManager : trustManagers)
        {
            try
            {
                trustManager.checkServerTrusted(chain, authType);
                return;
            }
            catch (CertificateException e)
            {
                // Continue checking other trust managers.
            }
        }

        throw new CertificateException("None of the TrustManagers trust this certificate chain");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        ImmutableList.Builder<X509Certificate> certificates = ImmutableList.builder();

        for (X509TrustManager trustManager : trustManagers)
            for (X509Certificate cert : trustManager.getAcceptedIssuers())
                certificates.add(cert);

        return Iterables.toArray(certificates.build(), X509Certificate.class);
    }

    public static TrustManager[] getTrustManagers(KeyStore keyStore)
        throws NoSuchAlgorithmException, KeyStoreException
    {
        return new TrustManager[] { new CompositeX509TrustManager(keyStore) };
    }

    public static X509TrustManager getDefaultTrustManager()
        throws NoSuchAlgorithmException, KeyStoreException
    {
        return getTrustManager(null);
    }

    public static X509TrustManager getTrustManager(KeyStore keystore)
        throws NoSuchAlgorithmException, KeyStoreException
    {
        return getTrustManager(TrustManagerFactory.getDefaultAlgorithm(), keystore);
    }

    public static X509TrustManager getTrustManager(String algorithm, KeyStore keystore)
        throws NoSuchAlgorithmException, KeyStoreException
    {
        TrustManagerFactory factory;
        factory = TrustManagerFactory.getInstance(algorithm);
        factory.init(keystore);

        return Iterables.getFirst(
            Iterables.filter(
                Arrays.asList(factory.getTrustManagers()),
                X509TrustManager.class),
            null);
    }
}
