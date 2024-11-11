package com.luminatehealth.fhir.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class PKCEUtil {

    private static final Map<String, String> localStorage = new HashMap<>();
    private static final int CODE_VERIFIER_LENGTH = 128;

    private static final Logger log = LoggerFactory.getLogger(PKCEUtil.class);


    public static String getCodeVerifier() {
        return localStorage.get("code_verifier");
    }

    public static String generateCodeChallenge() throws NoSuchAlgorithmException {
        String codeVerifier = generateCodeVerifier(CODE_VERIFIER_LENGTH);
        log.info("Code verifier generated: {}", codeVerifier);
        localStorage.put("code_verifier", codeVerifier);
        String codeChallenge = generateCodeChallengeFromVerifier(codeVerifier);
        return codeChallenge;
    }

    private static String generateCodeVerifier(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private static String generateCodeChallengeFromVerifier(String codeVerifier) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    public static void main(String[] args) {
        try {
            String codeChallenge = generateCodeChallenge();
            System.out.println("Generated Code Challenge: " + codeChallenge);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
