// src/main/java/com/gscorp/dv1/security/webauthn/web/PunchWithWebAuthnController.java
package com.gscorp.dv1.security.webauthn.web;

import java.lang.reflect.Constructor;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gscorp.dv1.security.webauthn.infrastructure.WebAuthnCredentialEntity;
import com.gscorp.dv1.security.webauthn.infrastructure.WebAuthnCredentialJpaRepository;
import com.gscorp.dv1.security.webauthn.support.AttestationStatementEnvelope;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.AttestedCredentialDataConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.credential.CredentialRecordImpl;
import com.webauthn4j.data.AuthenticationParameters; // (paquete .data en 0.29.6)
import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.statement.AttestationStatement;
import com.webauthn4j.data.client.CollectedClientData;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.data.extension.authenticator.AuthenticationExtensionsAuthenticatorOutputs;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientOutputs;
import com.webauthn4j.server.ServerProperty;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class PunchWithWebAuthnController {

  private static final String AUTH_CHALLENGE = "auth.challenge";
  private static final String AUTH_USER_ID   = "auth.userId";

  private final WebAuthnCredentialJpaRepository repo;
  private final WebAuthnManager webAuthn;
  private final ObjectConverter objectConverter;

  @PostMapping("/punch/options")
  public ResponseEntity<?> authOptions(HttpServletRequest req, Authentication auth) {
    Long userId = currentUserId(auth);
    var creds = repo.findByUserId(userId);
    if (creds.isEmpty()) return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body("Sin passkey");

    var challenge = new DefaultChallenge();
    HttpSession session = req.getSession(true);
    session.setAttribute(AUTH_CHALLENGE, challenge);
    session.setAttribute(AUTH_USER_ID, userId);

    var allow = creds.stream().map(c -> Map.of(
      "type", "public-key",
      "id",  b64u(c.getCredentialId())
    )).toList();

    return ResponseEntity.ok(Map.of(
      "challenge", b64u(challenge.getValue()),
      "rpId", effectiveRpId(req),
      "allowCredentials", allow,
      "userVerification", "required",
      "timeout", 60000
    ));
  }

  public record PunchReq(String assertionJSON, Double lat, Double lon, Double accuracy) {}

  @PostMapping("/punch/verify")
  public ResponseEntity<?> punch(@RequestBody PunchReq body,
                                 HttpServletRequest req,
                                 Authentication auth) {
    HttpSession session = req.getSession(false);
    var challenge = (DefaultChallenge) (session != null ? session.getAttribute(AUTH_CHALLENGE) : null);
    Long userId   = (Long) (session != null ? session.getAttribute(AUTH_USER_ID) : null);
    if (challenge == null || userId == null || !currentUserId(auth).equals(userId))
      return ResponseEntity.badRequest().body("Challenge inválido");

    // 1) Parse assertion
    var authData = webAuthn.parseAuthenticationResponseJSON(body.assertionJSON());
    byte[] credId = authData.getCredentialId();

    WebAuthnCredentialEntity cred = repo.findByUserIdAndCredentialId(userId, credId)
        .orElseThrow(() -> new IllegalArgumentException("Credencial no registrada"));

    // 2) Datos desde DB
    var cbor    = objectConverter.getCborConverter();
    var json    = objectConverter.getJsonConverter();
    var acdConv = new AttestedCredentialDataConverter(objectConverter);

    AttestedCredentialData attested = acdConv.convert(cred.getAttestedCredentialData());
    var envelope = cbor.readValue(cred.getAttestationStatementEnvelope(), AttestationStatementEnvelope.class);
    AttestationStatement attStmt = envelope.getAttestationStatement();

    AuthenticationExtensionsAuthenticatorOutputs<?> authnExt = null;
    if (cred.getAuthenticatorExtensionsCbor() != null) {
      authnExt = cbor.readValue(
          cred.getAuthenticatorExtensionsCbor(),
          AuthenticationExtensionsAuthenticatorOutputs.class
      );
    }

    AuthenticationExtensionsClientOutputs<?> clientExt = null;
    if (cred.getClientExtensionsJson() != null && !cred.getClientExtensionsJson().isBlank()) {
      clientExt = json.readValue(
          cred.getClientExtensionsJson(),
          AuthenticationExtensionsClientOutputs.class
      );
    }

    Set<AuthenticatorTransport> transports = null;
    if (cred.getTransportsJson() != null && !cred.getTransportsJson().isBlank()) {
      var names = json.readValue(cred.getTransportsJson(), new TypeReference<Set<String>>(){});
      transports = names.stream()
          .map(PunchWithWebAuthnController::toTransport)
          .filter(Objects::nonNull)
          .collect(Collectors.toSet());
    }

    // (Opcional, si agregaste estos campos en la entidad)
    AttestationObject aobj = null;
    CollectedClientData ccd = null;
    if (cred.getAttestationObjectCbor() != null) {
      aobj = cbor.readValue(cred.getAttestationObjectCbor(), AttestationObject.class);
    }
    if (cred.getCollectedClientDataJson() != null) {
      ccd = json.readValue(cred.getCollectedClientDataJson(), CollectedClientData.class);
    }

    // 3) Construir CredentialRecord con la sobrecarga disponible en tu JAR
    CredentialRecord credentialRecord = buildCredentialRecordFlexible(
        attested, attStmt, authnExt, clientExt, transports,
        cred.getSignCount(), /*uvInitialized*/ false, /*backedUp*/ Boolean.TRUE.equals(cred.getBackedUp()),
        aobj, ccd
    );

    // 4) ServerProperty y verificación
    Origin origin = effectiveOrigin(req);
    String rpId   = effectiveRpId(req);
    var serverProperty = new ServerProperty(origin, rpId, challenge);

    var params = new AuthenticationParameters(
        serverProperty,
        credentialRecord,
        null,   // allowCredentials (opcional)
        true,   // userVerificationRequired
        true    // userPresenceRequired
    );

    var result = webAuthn.verify(authData, params);

    // 5) Actualiza counter
    cred.setSignCount(result.getAuthenticatorData().getSignCount());
    repo.save(cred);

    // TODO: registra asistencia (IN/OUT) con body.lat/lon y retorna datos reales
    var ts = OffsetDateTime.now();
    if(session != null) {
    session.removeAttribute(AUTH_CHALLENGE);
    session.removeAttribute(AUTH_USER_ID);
    }
    return ResponseEntity.ok(Map.of("action","IN/OUT","ts",ts));
  }

  /** Intenta 8 → 6 → 4 parámetros para CredentialRecordImpl (según versión del JAR) */
  private static CredentialRecord buildCredentialRecordFlexible(
      AttestedCredentialData attested,
      AttestationStatement attStmt,
      AuthenticationExtensionsAuthenticatorOutputs<?> authnExt,
      AuthenticationExtensionsClientOutputs<?> clientExt,
      Set<AuthenticatorTransport> transports,
      long signCount,
      boolean uvInitialized,
      boolean backedUp,
      AttestationObject aobjOrNull,
      CollectedClientData ccdOrNull
  ) {

    Class<CredentialRecordImpl> cls = CredentialRecordImpl.class;

    // 8 argumentos
    try {
      Constructor<CredentialRecordImpl> c8 = cls.getConstructor(
          AttestedCredentialData.class,
          AttestationStatement.class,
          AuthenticationExtensionsAuthenticatorOutputs.class,
          AuthenticationExtensionsClientOutputs.class,
          Set.class,
          long.class,
          boolean.class,
          boolean.class
      );
      return c8.newInstance(attested, attStmt, authnExt, clientExt, transports, signCount, uvInitialized, backedUp);
    } catch (NoSuchMethodException ignore) {
      // sigue
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("No se pudo construir CredentialRecord (8 args): " + e.getMessage(), e);
    }

    // 6 argumentos
    try {
      Constructor<CredentialRecordImpl> c6 = cls.getConstructor(
          AttestedCredentialData.class,
          AttestationStatement.class,
          AuthenticationExtensionsAuthenticatorOutputs.class,
          AuthenticationExtensionsClientOutputs.class,
          Set.class,
          long.class
      );
      return c6.newInstance(attested, attStmt, authnExt, clientExt, transports, signCount);
    } catch (NoSuchMethodException ignore) {
      // sigue
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("No se pudo construir CredentialRecord (6 args): " + e.getMessage(), e);
    }

    // 4 argumentos (requiere aobj/ccd)
    if (aobjOrNull != null && ccdOrNull != null) {
      try {
        Constructor<CredentialRecordImpl> c4 = cls.getConstructor(
            AttestationObject.class,
            CollectedClientData.class,
            AuthenticationExtensionsClientOutputs.class,
            Set.class
        );
        return c4.newInstance(aobjOrNull, ccdOrNull, clientExt, transports);
      } catch (NoSuchMethodException ignore) {
        // sigue
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("No se pudo construir CredentialRecord (4 args): " + e.getMessage(), e);
      }
    }

    throw new IllegalStateException("No se encontró un constructor compatible de CredentialRecordImpl en este JAR.");
  }

  /* helpers */

  private static String b64u(byte[] bytes) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private static String effectiveRpId(HttpServletRequest req) {
    String xfHost = req.getHeader("X-Forwarded-Host");
    String host = (xfHost != null && !xfHost.isBlank()) ? xfHost : req.getServerName();
    int colon = host.indexOf(':');
    return (colon > 0) ? host.substring(0, colon) : host;
  }

  private static Origin effectiveOrigin(HttpServletRequest req) {
    String proto = headerOr(req, "X-Forwarded-Proto", req.getScheme());
    String host  = headerOr(req, "X-Forwarded-Host", req.getServerName());
    return new Origin(proto + "://" + host);
  }

  private static String headerOr(HttpServletRequest req, String name, String dflt) {
    String v = req.getHeader(name);
    return (v == null || v.isBlank()) ? dflt : v;
  }

  private Long currentUserId(Authentication auth) {
    return 1L; // TODO: reemplazar por el ID real del usuario autenticado
  }

  private static AuthenticatorTransport toTransport(String s) {
    if (s == null) return null;
    return switch (s.toLowerCase(java.util.Locale.ROOT)) {
      case "usb"      -> AuthenticatorTransport.USB;
      case "nfc"      -> AuthenticatorTransport.NFC;
      case "ble"      -> AuthenticatorTransport.BLE;
      case "internal" -> AuthenticatorTransport.INTERNAL;
      case "hybrid"   -> AuthenticatorTransport.HYBRID;
      default -> null;
    };
  }
}
