package com.zest.product.service.impl;

import com.zest.product.entity.RefreshToken;
import com.zest.product.exception.TokenRefreshException;
import com.zest.product.repository.RefreshTokenRepository;
import com.zest.product.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(String username) {
        // Revoke all existing refresh tokens for this user (rotation)
        refreshTokenRepository.revokeAllByUsername(username);

        RefreshToken refreshToken = RefreshToken.builder()
                .username(username)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException(token, "Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new TokenRefreshException(token, "Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new TokenRefreshException(token, "Refresh token has expired. Please login again");
        }

        // Rotate: revoke current token
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return refreshToken;
    }

    @Override
    @Transactional
    public void revokeRefreshTokensByUsername(String username) {
        refreshTokenRepository.revokeAllByUsername(username);
    }
}
