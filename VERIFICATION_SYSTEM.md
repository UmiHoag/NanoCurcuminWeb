# Hashed Verification Token System

## Overview

This implementation provides a secure, hashed verification code system for user email verification. The system uses SHA-256 hashing to ensure security and prevents token tampering.

## Security Features

### 1. **Hashed Tokens**
- All verification tokens are hashed using SHA-256 before storage
- Original tokens are never stored in the database
- Prevents token exposure even if database is compromised

### 2. **Token Expiration**
- Tokens expire after 24 hours by default
- Automatic cleanup of expired tokens
- Prevents replay attacks

### 3. **Multi-Parameter Validation**
- Tokens are validated against user email and user ID
- Prevents token reuse across different accounts
- Ensures token authenticity

### 4. **Secure Random Generation**
- Uses `SecureRandom` for cryptographically secure token generation
- 32-byte random tokens encoded in Base64
- Extremely low collision probability

## Database Schema Changes

The `User` entity has been enhanced with new fields:

```java
// New fields for hashed verification
private String hashedVerificationToken;
private LocalDateTime tokenExpiration;
private String emailVerificationHash;
private LocalDateTime emailVerificationExpiration;
```

## API Endpoints

### 1. **Email Verification via URL Parameters**
```
GET /api/v1/auth/verify-email?token={token}&email={email}&userId={userId}
```

**Parameters:**
- `token`: The verification token sent via email
- `email`: User's email address
- `userId`: User's ID

**Response:**
```json
{
  "message": "Email verified successfully!",
  "data": null
}
```

### 2. **Resend Verification Email**
```
POST /api/v1/auth/resend-verification/{userId}
```

**Response:**
```json
{
  "message": "Verification email sent successfully!",
  "data": null
}
```

### 3. **Legacy Verification (Maintained for Backward Compatibility)**
```
POST /api/v1/auth/verify
```

## How It Works

### 1. **User Registration**
When a user registers:
1. A secure random token is generated
2. The token is hashed using SHA-256 with email and userId
3. The hashed token and expiration time are stored in the database
4. The original token is sent via email

### 2. **Email Verification**
When a user clicks the verification link:
1. The system extracts token, email, and userId from URL parameters
2. The provided token is hashed with email and userId
3. The hash is compared with the stored hash
4. If valid and not expired, the user is marked as authenticated

### 3. **Security Validation**
The system validates:
- Token authenticity (hash match)
- Token expiration (within 24 hours)
- Email-user association (prevents cross-account use)
- User existence and status

## Example Usage

### 1. **User Registration Flow**
```java
// User registers
CreateUserRequest request = new CreateUserRequest();
request.setEmail("user@example.com");
request.setPassword("password123");
// ... other fields

User user = userService.createUser(request);
// Verification email is automatically sent
```

### 2. **Email Verification Flow**
```java
// User clicks link in email:
// http://localhost:8000/api/v1/auth/verify-email?token=abc123&email=user@example.com&userId=1

// System validates and verifies the user
userService.verifyEmailWithHashedToken("abc123", "user@example.com", 1L);
```

### 3. **Resend Verification**
```java
// If user needs a new verification email
String newToken = userService.generateNewVerificationToken(userId);
// New verification email is sent automatically
```

## Security Benefits

### 1. **Token Confidentiality**
- Original tokens are never stored in the database
- Even database compromise doesn't expose valid tokens
- Hashing is one-way (cannot be reversed)

### 2. **Tamper Resistance**
- Any modification to the token invalidates the hash
- Email and userId are part of the hash calculation
- Prevents token manipulation

### 3. **Expiration Control**
- Automatic token expiration prevents long-term attacks
- Configurable expiration time (default: 24 hours)
- Clean database state

### 4. **Cross-Account Protection**
- Tokens are tied to specific email-userId combinations
- Cannot be used across different accounts
- Prevents account takeover attempts

## Configuration

### Token Expiration
Modify the `TOKEN_EXPIRATION_HOURS` constant in `VerificationTokenService`:

```java
private static final int TOKEN_EXPIRATION_HOURS = 24; // Change as needed
```

### Hash Algorithm
The system uses SHA-256 by default. To change:

```java
private static final String HASH_ALGORITHM = "SHA-256"; // Change as needed
```

## Testing

Run the verification tests to ensure system functionality:

```bash
mvn test -Dtest=VerificationTokenServiceTest
```

The tests verify:
- Token generation and validation
- Email verification flow
- Security features
- Token uniqueness

## Error Handling

The system provides comprehensive error messages:

- `verification.parameters.cannot.be.null`: Missing required parameters
- `email.mismatch`: Email doesn't match user account
- `invalid.verification.token`: Token is invalid or expired
- `user.not.found.with.id`: User doesn't exist

## Migration from Legacy System

The new system maintains backward compatibility:
- Legacy `/verify` endpoint still works
- Old `verificationToken` field is preserved
- Gradual migration is possible

## Best Practices

1. **Always use HTTPS** in production for secure token transmission
2. **Monitor token usage** for suspicious patterns
3. **Implement rate limiting** on verification endpoints
4. **Log verification attempts** for security auditing
5. **Regular token cleanup** of expired tokens

## Troubleshooting

### Common Issues

1. **Token Expired**
   - Use resend verification endpoint
   - Check system clock synchronization

2. **Invalid Token**
   - Verify URL parameters are correct
   - Check for URL encoding issues

3. **Email Mismatch**
   - Ensure email in URL matches user account
   - Check for case sensitivity issues

### Debug Mode

Enable debug logging for token operations:

```properties
logging.level.com.nanoCurcuminWeb.service.verification=DEBUG
```

## Performance Considerations

- Token generation is fast (cryptographic operations)
- Database queries are optimized with indexes
- Minimal memory footprint for token storage
- Efficient hash comparison operations 