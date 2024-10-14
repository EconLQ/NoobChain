package com.liquiduspro;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public final class StringUtil {
    private static final String HEX_ARRAY = "0123456789ABCDEF";
    private static final Logger logger = Logger.getLogger(StringUtil.class.getName());

    public static String applySha256(final String input) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            final StringBuilder hexString = new StringBuilder(); // this will contain hash as a hexadecimal
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.warning(e.toString());
            throw new RuntimeException(e);
        }
    }
}
