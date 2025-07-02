package com.nanoCurcuminWeb.demo;

import com.nanoCurcuminWeb.service.verification.VerificationTokenService;
import org.springframework.stereotype.Component;

/**
 * Demonstration class showing how the hashed verification token system works
 */
@Component
public class VerificationDemo {

    private final VerificationTokenService verificationTokenService;

    public VerificationDemo(VerificationTokenService verificationTokenService) {
        this.verificationTokenService = verificationTokenService;
    }

    /**
     * Demonstrates the complete verification flow
     */
    public void demonstrateVerificationFlow() {
        System.out.println("=== Hashed Verification Token System Demo ===\n");

        // Step 1: User registration
        System.out.println("1. USER REGISTRATION");
        String userEmail = "john.doe@example.com";
        Long userId = 123L;
        System.out.println("   User Email: " + userEmail);
        System.out.println("   User ID: " + userId);

        // Step 2: Generate verification token
        System.out.println("\n2. GENERATE VERIFICATION TOKEN");
        VerificationTokenService.VerificationTokenData tokenData = 
            verificationTokenService.generateEmailVerificationToken(userEmail, userId);
        
        System.out.println("   Original Token: " + tokenData.getOriginalToken());
        System.out.println("   Hashed Token: " + tokenData.getHashedToken());
        System.out.println("   Expiration: " + tokenData.getExpiration());

        // Step 3: Create verification URL
        System.out.println("\n3. VERIFICATION URL");
        String verificationUrl = String.format(
            "http://localhost:8000/api/v1/auth/verify-email?token=%s&email=%s&userId=%d",
            tokenData.getOriginalToken(), userEmail, userId
        );
        System.out.println("   URL: " + verificationUrl);

        // Step 4: Validate token (simulating user clicking the link)
        System.out.println("\n4. TOKEN VALIDATION");
        boolean isValid = verificationTokenService.validateEmailVerificationToken(
            tokenData.getOriginalToken(),
            userEmail,
            userId,
            tokenData.getHashedToken(),
            tokenData.getExpiration()
        );
        System.out.println("   Token Valid: " + isValid);

        // Step 5: Demonstrate security features
        System.out.println("\n5. SECURITY FEATURES DEMONSTRATION");
        
        // Test with wrong email
        boolean wrongEmail = verificationTokenService.validateEmailVerificationToken(
            tokenData.getOriginalToken(),
            "wrong@example.com",
            userId,
            tokenData.getHashedToken(),
            tokenData.getExpiration()
        );
        System.out.println("   Wrong Email Validation: " + wrongEmail + " (should be false)");

        // Test with wrong user ID
        boolean wrongUserId = verificationTokenService.validateEmailVerificationToken(
            tokenData.getOriginalToken(),
            userEmail,
            999L,
            tokenData.getHashedToken(),
            tokenData.getExpiration()
        );
        System.out.println("   Wrong User ID Validation: " + wrongUserId + " (should be false)");

        // Test with wrong token
        boolean wrongToken = verificationTokenService.validateEmailVerificationToken(
            "wrong-token",
            userEmail,
            userId,
            tokenData.getHashedToken(),
            tokenData.getExpiration()
        );
        System.out.println("   Wrong Token Validation: " + wrongToken + " (should be false)");

        // Test with expired token
        java.time.LocalDateTime expiredTime = java.time.LocalDateTime.now().minusHours(1);
        boolean expiredToken = verificationTokenService.validateEmailVerificationToken(
            tokenData.getOriginalToken(),
            userEmail,
            userId,
            tokenData.getHashedToken(),
            expiredTime
        );
        System.out.println("   Expired Token Validation: " + expiredToken + " (should be false)");

        System.out.println("\n=== Demo Complete ===");
    }

    /**
     * Demonstrates token uniqueness and security
     */
    public void demonstrateTokenSecurity() {
        System.out.println("\n=== Token Security Demonstration ===\n");

        // Generate multiple tokens for the same user
        String email = "test@example.com";
        Long userId = 1L;

        System.out.println("Generating multiple tokens for user: " + email);
        
        for (int i = 1; i <= 3; i++) {
            VerificationTokenService.VerificationTokenData tokenData = 
                verificationTokenService.generateEmailVerificationToken(email, userId);
            
            System.out.println("\nToken " + i + ":");
            System.out.println("  Original: " + tokenData.getOriginalToken());
            System.out.println("  Hashed:   " + tokenData.getHashedToken());
        }

        System.out.println("\nKey Security Features:");
        System.out.println("✓ Each token is cryptographically unique");
        System.out.println("✓ Original tokens are never stored in database");
        System.out.println("✓ Hashing is one-way (cannot be reversed)");
        System.out.println("✓ Tokens expire automatically");
        System.out.println("✓ Tokens are tied to specific email-userId combinations");
    }
} 