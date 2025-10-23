package com.gscorp.dv1.auth.application;

import java.time.Duration;
import java.util.Optional;

import com.gscorp.dv1.auth.infrastructure.PasswordResetToken;
import com.gscorp.dv1.users.infrastructure.User;

public interface PasswordResetTokenService {

    PasswordResetToken createToken(User user, Duration duration);
    Optional<PasswordResetToken> findByToken(String token);
    boolean isValid(PasswordResetToken token);
    void markAsUsed(PasswordResetToken token);
}
