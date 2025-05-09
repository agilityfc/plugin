package org.agilityfc;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DonationRequest
{
    private static final MediaType MEDIA_TYPE = MediaType.parse("image/png");
    private static final String FILENAME = "screenshot.png";

    private static RequestBody screenshot(BufferedImage img)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try
        {
            ImageIO.write(img, "png", baos);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return RequestBody.create(MEDIA_TYPE, baos.toByteArray());
    }

    private static MultipartBody body(DonationInfo di)
    {
        return new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("from", di.getFrom())
            .addFormDataPart("to", di.getTo())
            .addFormDataPart("amount", Integer.toString(di.getAmount()))
            .addFormDataPart("screenshot", FILENAME, screenshot(di.getScreenshot()))
            .build();
    }

    public static Request.Builder builder(DonationInfo di)
    {
        assert di != null && di.getFrom() != null;
        return new Request.Builder().post(body(di));
    }
}
