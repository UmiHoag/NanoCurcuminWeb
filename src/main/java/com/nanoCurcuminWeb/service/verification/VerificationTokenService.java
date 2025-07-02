package com.nanoCurcuminWeb.service.verification;

import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class VerificationTokenService {
    
    private static final int TOKEN_EXPIRATION_HOURS = 24;
    private static final String HASH_ALGORITHM = "SHA-256";
    
    /**
     * Generates a secure random token and its hashed version
     * @return VerificationTokenData containing both original and hashed tokens
     */
    public VerificationTokenData generateVerificationToken() {
        String originalToken = generateSecureRandomToken();
        String hashedToken = hashToken(originalToken);
        LocalDateTime expiration = LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS);
        
        return new VerificationTokenData(originalToken, hashedToken, expiration);
    }
    
    /**
     * Generates a hashed verification token for email verification
     * @param email User's email address
     * @param userId User's ID
     * @return VerificationTokenData containing the hashed token
     */
    public VerificationTokenData generateEmailVerificationToken(String email, Long userId) {
        String originalToken = generateSecureRandomToken();
        String hashedToken = hashToken(originalToken + email + userId);
        LocalDateTime expiration = LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS);
        
        return new VerificationTokenData(originalToken, hashedToken, expiration);
    }
    
    /**
     * Validates a hashed token against the stored hash
     * @param providedToken The token provided by the user
     * @param storedHash The stored hashed token
     * @param expiration The token expiration time
     * @return true if token is valid and not expired
     */
    public boolean validateToken(String providedToken, String storedHash, LocalDateTime expiration) {
        if (providedToken == null || storedHash == null || expiration == null) {
            return false;
        }
        
        // Check if token is expired
        if (LocalDateTime.now().isAfter(expiration)) {
            return false;
        }
        
        // Validate the hash
        String hashedProvidedToken = hashToken(providedToken);
        return hashedProvidedToken.equals(storedHash);
    }
    
    /**
     * Validates an email verification token
     * @param providedToken The token provided by the user
     * @param email User's email
     * @param userId User's ID
     * @param storedHash The stored hashed token
     * @param expiration The token expiration time
     * @return true if token is valid and not expired
     */
    public boolean validateEmailVerificationToken(String providedToken, String email, Long userId, 
                                                 String storedHash, LocalDateTime expiration) {
        if (providedToken == null || storedHash == null || expiration == null || 
            email == null || userId == null) {
            return false;
        }
        
        // Check if token is expired
        if (LocalDateTime.now().isAfter(expiration)) {
            return false;
        }
        
        // Validate the hash
        String hashedProvidedToken = hashToken(providedToken + email + userId);
        return hashedProvidedToken.equals(storedHash);
    }
    
    /**
     * Generates a secure random token
     * @return Base64 encoded random token
     */
    private String generateSecureRandomToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    /**
     * Hashes a token using SHA-256
     * @param token The token to hash
     * @return Hashed token
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available", e);
        }
    }
    
    /**
     * Data class to hold verification token information
     */
    public static class VerificationTokenData {
        private final String originalToken;
        private final String hashedToken;
        private final LocalDateTime expiration;
        
        public VerificationTokenData(String originalToken, String hashedToken, LocalDateTime expiration) {
            this.originalToken = originalToken;
            this.hashedToken = hashedToken;
            this.expiration = expiration;
        }
        
        public String getOriginalToken() {
            return originalToken;
        }
        
        public String getHashedToken() {
            return hashedToken;
        }
        
        public LocalDateTime getExpiration() {
            return expiration;
        }
    }
} 