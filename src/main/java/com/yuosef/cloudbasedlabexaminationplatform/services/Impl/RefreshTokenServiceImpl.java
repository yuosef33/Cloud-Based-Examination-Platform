package com.yuosef.cloudbasedlabexaminationplatform.services.Impl;

import com.yuosef.cloudbasedlabexaminationplatform.repository.RefreshTokenDao;
import com.yuosef.cloudbasedlabexaminationplatform.models.RefreshToken;
import com.yuosef.cloudbasedlabexaminationplatform.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl {

    private final RefreshTokenDao refreshTokenRepository;

    @Value("${application.security.jwt.refresh-expiration}")
    private long refreshExpiration;

    // Create a new refresh token for the user
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RefreshToken createRefreshToken(User user) {
        // delete old one first so only one refresh token per user exists
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    // Find and validate the refresh token
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token expired, please login again");
        }

        return refreshToken;
    }

    // used on logout
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
