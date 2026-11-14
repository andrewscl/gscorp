package com.gscorp.dv1.auth.application;

import java.time.Duration;
import java.util.Optional;

import com.gscorp.dv1.auth.infrastructure.PasswordResetToken;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.enums.TokenPurpose;

public interface PasswordResetTokenService {

    PasswordResetToken createToken(
        User user,
        Duration duration,
        TokenPurpose purpose
    );

    Optional<PasswordResetToken> findByToken(String token);
    boolean isValid(PasswordResetToken token);
    void markAsUsed(PasswordResetToken token);
    void deleteByUserId(Long userId);

}
