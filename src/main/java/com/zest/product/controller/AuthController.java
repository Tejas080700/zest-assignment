package com.zest.product.controller;

import com.zest.product.dto.request.LoginRequest;
import com.zest.product.dto.request.RefreshTokenRequest;
import com.zest.product.dto.request.RegisterRequest;
import com.zest.product.dto.response.AuthResponse;
import com.zest.product.dto.response.MessageResponse;
import com.zest.product.entity.RefreshToken;
import com.zest.product.entity.Role;
import com.zest.product.entity.User;
import com.zest.product.exception.BadRequestException;
import com.zest.product.repository.UserRepository;
import com.zest.product.security.JwtTokenProvider;
import com.zest.product.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and return JWT tokens")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getUsername());

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .username(request.getUsername())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register", description = "Register a new user account")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String role : request.getRoles()) {
                try {
                    roles.add(Role.valueOf("ROLE_" + role.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Invalid role: " + role);
                }
            }
        } else {
            roles.add(Role.ROLE_USER);
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .enabled(true)
                .build();

        userRepository.save(user);

        return new ResponseEntity<>(
                new MessageResponse("User registered successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Get new access token using refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());

        String newAccessToken = jwtTokenProvider.generateAccessToken(refreshToken.getUsername());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(refreshToken.getUsername());

        AuthResponse response = AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .username(refreshToken.getUsername())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoke all refresh tokens for the user")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        refreshTokenService.revokeRefreshTokensByUsername(refreshToken.getUsername());
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }
}
