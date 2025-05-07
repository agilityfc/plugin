package org.agilityfc;

import lombok.NonNull;
import lombok.Value;
import lombok.With;

import java.awt.image.BufferedImage;

@Value
public class DonationInfo
{
    /// Display name of the donor.
    @With
    String from;

    /// Display name of the receiver.
    @NonNull
    String to;

    /// A non-negative amount of GP.
    int amount;

    /// Screenshot of the trade, loot key or price check screen.
    @NonNull
    BufferedImage screenshot;
}
