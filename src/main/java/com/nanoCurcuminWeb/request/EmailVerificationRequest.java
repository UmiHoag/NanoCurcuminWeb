package com.nanoCurcuminWeb.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailVerificationRequest {
    
    @NotBlank(message = "Token is required")
    private String token;
} 