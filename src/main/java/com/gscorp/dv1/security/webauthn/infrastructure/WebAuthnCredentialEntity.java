// src/main/java/com/gscorp/dv1/security/webauthn/infrastructure/WebAuthnCredentialEntity.java
package com.gscorp.dv1.security.webauthn.infrastructure;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(
  name = "webauthn_credentials",
  indexes = @Index(name = "idx_webauthn_user", columnList = "user_id")
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Builder
public class WebAuthnCredentialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Lob
    @Column(name = "credential_id", nullable = false, unique = true, columnDefinition = "bytea")
    private byte[] credentialId;

    // opcional (si quieres mantener compatibilidad)
    @Lob
    @Column(name = "public_key_cose", columnDefinition = "bytea")
    private byte[] publicKeyCose;

    @Column(name = "sign_count", nullable = false, columnDefinition = "bigint not null default 0")
    private long signCount;

    @Column(name = "backed_up")
    private Boolean backedUp;

    @Column(name = "transports", length = 64)
    private String transports;

    @Column(
        name = "created_at",
        nullable = false,
        columnDefinition = "timestamptz not null default now()",
        insertable = false,
        updatable = false
    )
    private OffsetDateTime createdAt;

    /* ---- NUEVO: persistir todo para CredentialRecord ---- */

    @Lob @Column(name = "attested_credential_data", columnDefinition = "bytea")
    private byte[] attestedCredentialData;

    @Lob @Column(name = "attestation_stmt_envelope", columnDefinition = "bytea")
    private byte[] attestationStatementEnvelope;

    @Lob @Column(name = "client_extensions_json")
    private String clientExtensionsJson;

    @Lob @Column(name = "authenticator_extensions_cbor", columnDefinition = "bytea")
    private byte[] authenticatorExtensionsCbor;

    @Lob @Column(name = "transports_json")
    private String transportsJson;

    @Lob @Column(name = "aaguid", columnDefinition = "bytea")
    private byte[] aaguid;

    @PrePersist
    void onCreate(){
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    // --- NUEVOS CAMPOS PARA RECONSTRUCCIÓN SENCILLA ---
    @Lob
    @Column(name = "attestation_object_cbor", columnDefinition = "bytea")
    private byte[] attestationObjectCbor;

    @Column(name = "collected_client_data_json", columnDefinition = "text")
    private String collectedClientDataJson;


}
