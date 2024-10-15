package com.liquiduspro.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public final class StringUtil {
    private static final Logger logger = LoggerFactory.getLogger(StringUtil.class);

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
            logger.warn("Error while hashing string: {}", e.toString());
            throw new RuntimeException(e);
        }
    }
}
