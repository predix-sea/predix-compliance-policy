package com.predix.compliance.audit;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class IpMaskingUtil {

    private IpMaskingUtil() {}

    public static String mask(String ip) {
        if (ip == null || ip.isBlank()) {
            return null;
        }
        if (ip.contains(".")) {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + ".xxx.xxx";
            }
        }
        if (ip.contains(":")) {
            int idx = ip.lastIndexOf(':');
            return idx > 0 ? ip.substring(0, idx) + ":xxxx" : "xxxx";
        }
        return "xxx";
    }

    public static String hash(String ip) {
        if (ip == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(ip.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
