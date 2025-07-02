package com.nanoCurcuminWeb.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "Invalid credentials")
    private String identifier;

    @NotBlank(message = "Invalid credentials")
    private String password;
}
