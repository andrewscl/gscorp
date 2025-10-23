package com.gscorp.dv1.auth.application;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.auth.infrastructure.PasswordResetToken;
import com.gscorp.dv1.auth.infrastructure.PasswordResetTokenRepository;
import com.gscorp.dv1.users.infrastructure.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final PasswordResetTokenRepository tokenRepo;

    @Override
    public PasswordResetToken createToken(User user, Duration duration) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plus(duration);

        PasswordResetToken prt = new PasswordResetToken(token, user, expiry);
        return tokenRepo.save(prt);
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return tokenRepo.findByToken(token);
    }

    @Override
    public boolean isValid(PasswordResetToken token) {
        return token != null && !token.isExpired() && !token.isUsed();
    }

    @Override
    public void markAsUsed(PasswordResetToken token) {
        token.setUsed(true);
        token.setUsedAt(LocalDateTime.now());
        tokenRepo.save(token);
    }

    @Override
    public void deleteByUserId(Long userId) {
        tokenRepo.deleteByUserId(userId);
    }

}
