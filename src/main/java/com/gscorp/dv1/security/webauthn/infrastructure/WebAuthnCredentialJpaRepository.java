// src/main/java/com/gscorp/dv1/security/webauthn/infrastructure/WebAuthnCredentialJpaRepository.java
package com.gscorp.dv1.security.webauthn.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface WebAuthnCredentialJpaRepository
    extends JpaRepository<WebAuthnCredentialEntity, Long> {

  List<WebAuthnCredentialEntity> findByUserId(Long userId);
  Optional<WebAuthnCredentialEntity> findByUserIdAndCredentialId(Long userId, byte[] credentialId);
  Optional<WebAuthnCredentialEntity> findByCredentialId(byte[] credentialId);
}
