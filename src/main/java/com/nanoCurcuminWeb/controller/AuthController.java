package com.nanoCurcuminWeb.controller;

import com.nanoCurcuminWeb.annotation.RateLimited;
import com.nanoCurcuminWeb.request.EmailVerificationRequest;
import com.nanoCurcuminWeb.request.LoginRequest;
import com.nanoCurcuminWeb.response.ApiResponse;
import com.nanoCurcuminWeb.response.JwtResponse;
import com.nanoCurcuminWeb.security.jwt.JwtUtils;
import com.nanoCurcuminWeb.security.user.ShopUserDetails;
import com.nanoCurcuminWeb.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final MessageSource messageSource;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    request.getIdentifier(), request.getPassword());

            Authentication authentication = authenticationManager.authenticate(authToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateTokenForUser(authentication);
            ShopUserDetails userDetails = (ShopUserDetails) authentication.getPrincipal();
            JwtResponse jwtResponse = new JwtResponse(userDetails.getId(), jwt);

            return ResponseEntity.ok(new ApiResponse(
                    messageSource.getMessage("valid.credentials", null, LocaleContextHolder.getLocale()), jwtResponse));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(
                            messageSource.getMessage("invalid.credentials", null, LocaleContextHolder.getLocale()),
                            null));
        }
    }

    @PostMapping("/verify")
    @RateLimited("email-verification")
    public ResponseEntity<String> verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
        try {
            userService.verifyEmail(request.getToken());
            return ResponseEntity
                    .ok(messageSource.getMessage("valid.verification.email", null, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(messageSource.getMessage("invalid.verification.email",
                    null, LocaleContextHolder.getLocale()));
        }
    }
}
