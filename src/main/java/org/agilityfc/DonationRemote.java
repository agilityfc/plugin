package org.agilityfc;

import lombok.NonNull;
import lombok.Value;

@Value
public class DonationRemote
{
    /// The URL of an HTTP(S) endpoint.
    @NonNull
    String url;

    /// A PEM-encoded SSL certificate to trust in case {@see url} is an HTTPS
    /// endpoint with a self-signed certificate, otherwise null.
    String cert;
}
