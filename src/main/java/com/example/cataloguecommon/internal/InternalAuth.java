package com.example.cataloguecommon.internal;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Proves a request came from a trusted internal service (the BFF, the UI, the
 * crawler) and, for account-scoped calls, which user it is acting for.
 *
 * <p>Header {@value #HEADER}: {@code t=<epochMillis>,u=<actingUserId|->,s=<hex hmac>}
 * where the HMAC is {@code HMAC-SHA256(secret, "<METHOD>\n<path>\n<u>\n<t>")}.
 * Verification recomputes it, compares in constant time, and rejects a timestamp
 * skew beyond {@link #MAX_SKEW_MS} (a small replay window; internal traffic only).
 *
 * <p>Deliberately a per-service copy — it crosses the BFF/UI boundary the
 * architecture keeps decoupled. Pure JDK crypto, no Spring.
 */
public final class InternalAuth {

    public static final String HEADER = "X-Internal-Auth";
    public static final long MAX_SKEW_MS = 5 * 60 * 1000;
    /** {@code u} value when the caller isn't acting for a specific user (register, crawler, …). */
    public static final String NO_USER = "-";

    private InternalAuth() {
    }

    /** Build the header value for an outbound call. {@code actingUser} may be null. */
    public static String header(String secret, String method, String path, Long actingUser) {
        long t = System.currentTimeMillis();
        String u = actingUser == null ? NO_USER : Long.toString(actingUser);
        String sig = hmac(secret, canonical(method, path, u, t));
        return "t=" + t + ",u=" + u + ",s=" + sig;
    }

    /**
     * Verify an inbound header. Returns the acting user id, or {@code null} for
     * {@link #NO_USER}. Throws {@link InvalidInternalAuthException} if the header
     * is missing, malformed, stale, or the signature doesn't match.
     */
    public static Long verify(String secret, String headerValue, String method, String path) {
        if (headerValue == null || headerValue.isBlank()) {
            throw new InvalidInternalAuthException("missing " + HEADER);
        }
        String t = null, u = null, s = null;
        for (String part : headerValue.split(",")) {
            int eq = part.indexOf('=');
            if (eq < 0) {
                continue;
            }
            String k = part.substring(0, eq).trim();
            String v = part.substring(eq + 1).trim();
            switch (k) {
                case "t" -> t = v;
                case "u" -> u = v;
                case "s" -> s = v;
                default -> { /* ignore unknown */ }
            }
        }
        if (t == null || u == null || s == null) {
            throw new InvalidInternalAuthException("malformed " + HEADER);
        }
        long ts;
        try {
            ts = Long.parseLong(t);
        } catch (NumberFormatException e) {
            throw new InvalidInternalAuthException("bad timestamp");
        }
        if (Math.abs(System.currentTimeMillis() - ts) > MAX_SKEW_MS) {
            throw new InvalidInternalAuthException("stale request");
        }
        String expected = hmac(secret, canonical(method, path, u, ts));
        if (!constantTimeEquals(expected, s)) {
            throw new InvalidInternalAuthException("signature mismatch");
        }
        return NO_USER.equals(u) ? null : Long.parseLong(u);
    }

    private static String canonical(String method, String path, String u, long t) {
        return method.toUpperCase() + "\n" + path + "\n" + u + "\n" + t;
    }

    private static String hmac(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC failure", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    /** Thrown on a missing / bad internal-auth header — map to 403. */
    public static class InvalidInternalAuthException extends RuntimeException {
        public InvalidInternalAuthException(String message) {
            super(message);
        }
    }
}
