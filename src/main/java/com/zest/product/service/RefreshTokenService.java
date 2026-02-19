package com.zest.product.service;

import com.zest.product.entity.RefreshToken;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(String username);

    RefreshToken verifyRefreshToken(String token);

    void revokeRefreshTokensByUsername(String username);
}
