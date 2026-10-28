package org.ecom.econwaai.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for one-way cryptographic hashing to enforce NDPA 2023 zero-raw storage compliance.
 */
public final class HashUtil {

    private HashUtil() {
    }

    /**
     * Hashes an identifier (e.g., NIN, BVN) with SHA-256 and returns a lowercase hex string.
     * Raw inputs are never logged or stored.
     *
     * @param input Raw sensitive string (e.g. 11-digit NIN)
     * @return 64-character lowercase hexadecimal SHA-256 hash, or null if input is null/blank
     */
    public static String sha256Hex(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available in environment", e);
        }
    }
}
