package org.agilityfc;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.util.Text;
import org.agilityfc.util.ClientUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DonationScraper
{
    private static final Pattern TRADE_OPPONENT_PAT =
        Pattern.compile("(?i)trading with: ?(.+)");
    private static final Pattern TRADE_VALUE_PAT =
        Pattern.compile("(?i)value: (.+) coins");
    private static final Pattern LOOT_KEY_VALUE_PAT =
        Pattern.compile("(?i)value in chest: (.+)gp");
    private static final Pattern PRICE_CHECK_VALUE_PAT =
        Pattern.compile("(?i)total guide price: ?(.+)");

    @Inject
    private Client client;

    @Inject
    private ClientUtil clientUtil;

    private static String findWidgetText(Widget w, Pattern pat)
    {
        String text = w.getText();
        if (text == null) return null;

        Matcher m = pat.matcher(Text.removeTags(text));
        return m.find() ? m.group(1) : null;
    }

    private static int parseValue(String value)
    {
        return Integer.parseInt(value.replaceAll(",", ""));
    }

    private static Rectangle scaleBounds(
        Rectangle bounds, double scaleX, double scaleY)
    {
        return new Rectangle(
            (int) (scaleX * bounds.x),
            (int) (scaleY * bounds.y),
            (int) (scaleX * bounds.width),
            (int) (scaleY * bounds.height));
    }

    private BufferedImage cropScreenshot(BufferedImage img, Rectangle bounds)
    {
        if (client.isStretchedEnabled())
        {
            Dimension real = client.getRealDimensions();
            Dimension stretched = client.getStretchedDimensions();

            bounds = scaleBounds(
                bounds,
                stretched.getWidth() / real.getWidth(),
                stretched.getHeight() / real.getHeight());
        }

        return img.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public DonationInfo scrapeDonation(
        int containerId,
        int opponentId, Pattern opponentPat,
        int valueId, Pattern valuePat)
    {
        assert !client.isClientThread();

        var t = clientUtil.runOnClientThread(() ->
        {
            Player p = client.getLocalPlayer();
            if (p == null) return null;

            Widget w = client.getWidget(containerId);
            if (w == null || w.isHidden()) return null;

            BiFunction<Integer, Pattern, String> extract = (id, pat) ->
            {
                Widget w2 = client.getWidget(id);
                assert w2 != null && !w2.isHidden();

                String name = findWidgetText(w2, pat);
                assert name != null;

                return name;
            };

            return new ImmutableTriple<>(
                new ImmutablePair<>(
                    opponentId != -1 ? extract.apply(opponentId, opponentPat) : null,
                    p.getName()),
                extract.apply(valueId, valuePat),
                w.getBounds());
        });

        if (t == null) return null;

        return new DonationInfo(
            t.getLeft().getLeft(),
            t.getLeft().getRight(),
            parseValue(t.getMiddle()),
            cropScreenshot(clientUtil.takeScreenshot(), t.getRight()));
    }

    public DonationInfo scrapeDonation(
        int containerId, int valueId, Pattern valuePat)
    {
        return scrapeDonation(containerId, -1, null, valueId, valuePat);
    }

    public DonationInfo scrapeTradeMainScreen()
    {
        return scrapeDonation(
            InterfaceID.Trademain.UNIVERSE,
            InterfaceID.Trademain.TITLE, TRADE_OPPONENT_PAT,
            InterfaceID.Trademain.OTHER_OFFER_HEADER, TRADE_VALUE_PAT);
    }

    public DonationInfo scrapeTradeConfirmScreen()
    {
        return scrapeDonation(
            InterfaceID.Tradeconfirm.UNIVERSE,
            InterfaceID.Tradeconfirm.TRADEOPPONENT, TRADE_OPPONENT_PAT,
            InterfaceID.Tradeconfirm.YOU_WILL_RECEIVE, TRADE_VALUE_PAT);
    }

    public DonationInfo scrapeLootKeyScreen()
    {
        return scrapeDonation(
            InterfaceID.WildyLootChest.CONTENTS,
            InterfaceID.WildyLootChest.VALUE, LOOT_KEY_VALUE_PAT);
    }

    public DonationInfo scrapePriceCheckScreen()
    {
        return scrapeDonation(
            InterfaceID.GePricechecker.UNIVERSE,
            InterfaceID.GePricechecker.OUTPUT, PRICE_CHECK_VALUE_PAT);
    }

    public DonationInfo scrapeDonation()
    {
        List<Supplier<DonationInfo>> scrapers = List.of(
            this::scrapeTradeMainScreen,
            this::scrapeTradeConfirmScreen,
            this::scrapeLootKeyScreen,
            this::scrapePriceCheckScreen);

        for (var s : scrapers)
        {
            DonationInfo di = s.get();
            if (di != null) return di;
        }

        return null;
    }
}
