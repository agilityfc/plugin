package org.agilityfc;

import lombok.SneakyThrows;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;

public class ClientUtil
{
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private DrawManager drawManager;

    @SneakyThrows
    public <T> T runOnClientThread(Callable<T> c)
    {
        FutureTask<T> r = new FutureTask<>(c);
        clientThread.invoke(r);
        return r.get();
    }

    @SneakyThrows
    public BufferedImage takeScreenshot()
    {
        assert !client.isClientThread();

        CompletableFuture<BufferedImage> r = new CompletableFuture<>();
        drawManager.requestNextFrameListener(
            img -> r.complete(ImageUtil.bufferedImageFromImage(img)));

        return r.get();
    }
}
