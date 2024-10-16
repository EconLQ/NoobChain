package com.liquiduspro.util;


import com.liquiduspro.domain.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;


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

    // apply ECDSA signature and return the result in bytes
    public static byte[] applyECDSASignature(final PrivateKey privateKey, final String input) {
        Signature ecdsa;
        byte[] output;
        try {
            ecdsa = Signature.getInstance("ECDSA", "BC");
            ecdsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            ecdsa.update(strByte);
            output = ecdsa.sign();  // real signature
        } catch (NoSuchAlgorithmException | SignatureException | NoSuchProviderException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    // verify ECDSA signature
    public static boolean verifyECDSASignature(final PublicKey publicKey, final String data, final byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getStringFromKey(final Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String getMerkleRoot(final List<Transaction> transactions) {
        int count = transactions.size();
        List<String> previousTreeLayer = transactions
                .stream()
                .map(Transaction::getTransactionId)
                .collect(Collectors.toList());

        List<String> treeLayer = new ArrayList<>();
        while (count > 1) {
            if (count % 2 != 0) {
                treeLayer.add(previousTreeLayer.get(count - 1));
                count--;
            }
            for (int i = 0; i < count; i += 2) {
                String parent = applySha256(previousTreeLayer.get(i) + previousTreeLayer.get(i + 1));
                treeLayer.add(parent);
            }
            previousTreeLayer = treeLayer;
            treeLayer = new ArrayList<>();
            count = previousTreeLayer.size();
        }
        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }
}
