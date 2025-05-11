package org.agilityfc;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.util.ImageCapture;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.QuantityFormatter;
import net.runelite.client.util.Text;
import okhttp3.Call;
import okhttp3.Response;
import org.agilityfc.util.GridBagConstraintsBuilder;
import org.agilityfc.util.NameAutocompleter;

import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class AgilityFcPanel extends PluginPanel
{
    private static final int MAX_USERNAME_LENGTH = 12;

    private static final Dimension STANDARD_DIM = new Dimension(
        PluginPanel.PANEL_WIDTH - 20, 30);

    private static final Dimension SCREENSHOT_DIM = new Dimension(
        PluginPanel.PANEL_WIDTH - 20, (int) (0.75 * PluginPanel.PANEL_WIDTH));

    private static final BufferedImage AGILITY_ICON =
        ImageUtil.loadImageResource(AgilityFcPlugin.class, "icon.png");

    private static final String SCREENSHOT_DIR = "AFC";

    @Inject
    private ImageCapture imageCapture;

    @Inject
    private DonationScraper scraper;

    @Inject
    private AgilityFcPlugin plugin;

    private DonationInfo scrapedDono;
    private SendWorker sendWorker;
    private final IconTextField fromField;
    private final JTextField toField;
    private final JTextField amountField;
    private final JLabel screenshotLabel;
    private final JButton scrapeButton;
    private final JButton sendButton;

    private static JPanel labeledComponent(String name, JComponent component)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel(name);
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setBorder(new EmptyBorder(0, 0, 4, 0));
        label.setForeground(Color.WHITE);

        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);

        return panel;
    }

    private static BufferedImage fitImage(BufferedImage img, Dimension dim)
    {
        int iw = img.getWidth();
        int ih = img.getHeight();
        int dw = (int) dim.getWidth();
        int dh = (int) dim.getHeight();

        if (iw == dw && ih == dh) return img;

        if (iw > dw || ih > dh)
        {
            double fact = Math.min((double) dw / iw, (double) dh / ih);

            AffineTransform xform = AffineTransform.getScaleInstance(fact, fact);
            AffineTransformOp op = new AffineTransformOp(
                xform, AffineTransformOp.TYPE_BILINEAR);

            img = op.filter(img, null);
            iw = img.getWidth();
            ih = img.getHeight();
        }

        BufferedImage r = new BufferedImage(dw, dh, BufferedImage.TYPE_INT_ARGB);
        Graphics g = r.getGraphics();
        g.drawImage(img, (dw - iw) / 2, (dh - ih) / 2, null);
        g.dispose();

        return r;
    }

    private static void copyString(String s)
    {
        Toolkit.getDefaultToolkit().getSystemClipboard()
            .setContents(new StringSelection(s), null);
    }

    private static String screenshotName(DonationInfo di)
    {
        assert di.getFrom() != null;
        return String.format("Donation (%s) (%s)",
            di.getFrom(),
            QuantityFormatter.quantityToRSDecimalStack(di.getAmount(), true)
                .toLowerCase());
    }

    private static boolean isNameValid(String from)
    {
        String clean = Text.toJagexName(from);
        int length = clean.length();

        return from.length() == clean.length() &&
            length > 0 &&
            length <= MAX_USERNAME_LENGTH;
    }

    private void setScreenshot(BufferedImage img)
    {
        screenshotLabel.setIcon(new ImageIcon(fitImage(img, SCREENSHOT_DIM)));
    }

    private void clearDonation()
    {
        scrapedDono = null;
        fromField.setText("");
        toField.setText("");
        amountField.setText("");
        setScreenshot(AGILITY_ICON);
    }

    private void scrapeDonation()
    {
        DonationInfo di = scraper.scrapeDonation();
        if (di == null) return;

        // Use the scraped `from` if present, otherwise keep the current one.
        String scrapedFrom = di.getFrom();
        String from = scrapedFrom != null ? scrapedFrom : fromField.getText();

        // Reset the icon if the `from` name is now valid.
        if (isNameValid(from)) fromField.setIcon(IconTextField.Icon.SEARCH);

        scrapedDono = di;
        fromField.setText(from);
        toField.setText(di.getTo());
        amountField.setText(QuantityFormatter.formatNumber(di.getAmount()));
        setScreenshot(di.getScreenshot());
        sendButton.setEnabled(true);
    }

    class SendWorker extends SwingWorker<Void, Void>
    {
        private final DonationInfo di;
        private final CompletableFuture<Call> future;

        public SendWorker(DonationInfo di)
        {
            this.di = di;
            this.future = new CompletableFuture<>();
        }

        public DonationInfo getDonationInfo()
        {
            return di;
        }

        public void cancel()
        {
            cancel(true);
            future.thenAccept(Call::cancel);
        }

        @Override
        protected Void doInBackground() throws Exception
        {
            Call call = plugin.makeCall(di);
            future.complete(call);

            try (Response r = call.execute())
            {
                if (r.code() != 200)
                {
                    throw new RuntimeException(
                        String.format("Unexpected response code: %s", r.code()));
                }
            }

            return null;
        }

        private void saveScreenshot(DonationInfo di, boolean success)
        {
            CompletableFuture.runAsync(() -> {
                String name = screenshotName(di);
                imageCapture.saveScreenshot(
                    di.getScreenshot(), success ? name : name + " (Failed)",
                    SCREENSHOT_DIR, true, true);
            });
        }

        @Override
        protected void done()
        {
            fromField.setEditable(true);
            scrapeButton.setEnabled(true);

            try
            {
                get();
                fromField.setIcon(IconTextField.Icon.SEARCH);
                clearDonation();
                saveScreenshot(di, true);
            }
            catch (InterruptedException | ExecutionException |
                   CancellationException e)
            {
                log.error("Send failed", e);
                fromField.setIcon(IconTextField.Icon.ERROR);
                sendButton.setEnabled(true);
                saveScreenshot(di, false);
            }
        }
    }

    private void sendDonation()
    {
        assert scrapedDono != null;
        String from = fromField.getText();

        if (isNameValid(from))
        {
            fromField.setIcon(IconTextField.Icon.LOADING);
            fromField.setEditable(false);
            scrapeButton.setEnabled(false);
            sendButton.setEnabled(false);

            DonationInfo di = scrapedDono.withFrom(from);
            sendWorker = new SendWorker(di);
            sendWorker.execute();
        }
        else
        {
            fromField.setIcon(IconTextField.Icon.ERROR);
        }
    }

    @Inject
    public AgilityFcPanel(
        NameAutocompleter nameAutocompleter, AgilityFcConfig config)
    {
        setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new GridBagLayout());

        fromField = new IconTextField();
        fromField.setIcon(IconTextField.Icon.SEARCH);
        fromField.setPreferredSize(STANDARD_DIM);
        fromField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        fromField.addKeyListener(nameAutocompleter);
        fromField.addActionListener(e ->
        {
            // NOTE: Toggle the editable status to end the autocomplete.
            fromField.setEditable(false);
            fromField.setEditable(true);
        });
        fromField.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() != 2) return;
                fromField.setText("AFC always wins");
            }
        });
        fromField.addClearListener(() ->
        {
            if (sendWorker == null)
            {
                fromField.setIcon(IconTextField.Icon.SEARCH);
            }
            else
            {
                sendWorker.cancel();

                if (sendWorker.isCancelled())
                {
                    // NOTE: Set back the cleared text and leave it up to the
                    // worker's `done()` method to do the rest.
                    fromField.setText(sendWorker.getDonationInfo().getFrom());
                }
                else
                {
                    fromField.setIcon(IconTextField.Icon.SEARCH);
                }

                sendWorker = null;
            }
        });

        toField = new JTextField();
        toField.setPreferredSize(STANDARD_DIM);
        toField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        toField.setEditable(false);

        amountField = new JTextField();
        amountField.setPreferredSize(STANDARD_DIM);
        amountField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        amountField.setEditable(false);

        screenshotLabel = new JLabel();
        screenshotLabel.setHorizontalAlignment(SwingConstants.CENTER);

        scrapeButton = new JButton("Scrape");
        scrapeButton.addActionListener(e -> scrapeDonation());
        scrapeButton.setPreferredSize(STANDARD_DIM);

        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendDonation());
        sendButton.setPreferredSize(STANDARD_DIM);
        sendButton.setEnabled(false);

        JButton copyKeyButton = new JButton("Copy key");
        copyKeyButton.addActionListener(e -> copyString(config.key()));
        copyKeyButton.setPreferredSize(STANDARD_DIM);

        GridBagConstraintsBuilder b = new GridBagConstraintsBuilder()
            .x(0)
            .insets(3, 0, 3, 0)
            .fill(GridBagConstraints.HORIZONTAL);

        add(labeledComponent("From", fromField), b.y(0).build());
        add(labeledComponent("To", toField), b.y(1).build());
        add(labeledComponent("Amount", amountField), b.y(2).build());
        add(labeledComponent("Screenshot", screenshotLabel), b.y(3).build());
        add(scrapeButton, b.y(4).build());
        add(sendButton, b.y(5).build());
        add(new JSeparator(), b.y(6).build());
        add(copyKeyButton, b.y(7).build());

        clearDonation();
    }
}
