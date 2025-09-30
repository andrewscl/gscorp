// src/main/java/com/gscorp/dv1/security/webauthn/web/PasskeyController.java
package com.gscorp.dv1.security.webauthn.web;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.gscorp.dv1.security.webauthn.infrastructure.*;
import com.gscorp.dv1.security.webauthn.support.AttestationStatementEnvelope;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.AttestedCredentialDataConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.*;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/passkeys")
@RequiredArgsConstructor
public class PasskeyController {

  private static final String REG_CHALLENGE = "reg.challenge";
  private static final String REG_USER_ID   = "reg.userId";

  private final WebAuthnCredentialJpaRepository repo;
  private final WebAuthnManager webAuthn;
  private final ObjectConverter objectConverter;

  @PostMapping(value = "/register/options", produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, Object> registerOptions(HttpServletRequest req, Authentication auth) {
    Long userId = currentUserId(auth);
    String rpId = effectiveRpId(req);
    var challenge = new DefaultChallenge();

    HttpSession session = req.getSession(true);
    session.setAttribute(REG_CHALLENGE, challenge);
    session.setAttribute(REG_USER_ID, userId);

    var pubKeyCredParams = List.of(
      Map.of("type", "public-key", "alg", -7),   // ES256
      Map.of("type", "public-key", "alg", -257)  // RS256
    );

    // excludeCredentials: evita registrar misma credencial otra vez
    var exclude = repo.findByUserId(userId).stream().map(c -> Map.of(
      "type","public-key",
      "id",  b64u(c.getCredentialId())
    )).toList();

    return Map.of(
      "rp", Map.of("id", rpId, "name", "GSCorp"),
      "user", Map.of(
        "id", b64u(Long.toString(userId).getBytes(StandardCharsets.UTF_8)),
        "name", "user-" + userId,
        "displayName", "User " + userId
      ),
      "challenge", b64u(challenge.getValue()),
      "pubKeyCredParams", pubKeyCredParams,
      "attestation", "none",
      "authenticatorSelection", Map.of("residentKey", "preferred", "userVerification", "required"),
      "excludeCredentials", exclude,
      "timeout", 60000
    );
  }

    @PostMapping(value = "/register/verify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerVerify(@RequestBody String attestationJSON,
                                            HttpServletRequest req,
                                            Authentication auth) {
        HttpSession session = req.getSession(false);
        if (session == null) return ResponseEntity.badRequest().body(Map.of("error","Sesión no encontrada"));

        var challenge = (DefaultChallenge) session.getAttribute(REG_CHALLENGE);
        Long userId   = (Long) session.getAttribute(REG_USER_ID);
        if (challenge == null || userId == null || !currentUserId(auth).equals(userId))
            return ResponseEntity.badRequest().body(Map.of("error", "Challenge inválido"));

        try {
            Origin origin = effectiveOrigin(req);
            String rpId   = effectiveRpId(req);
            var serverProperty = new ServerProperty(origin, rpId, challenge);

            var pubKeyCredParams = List.of(
                new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256),
                new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.RS256)
            );

            var regData = webAuthn.verifyRegistrationResponseJSON(
                attestationJSON,
                new RegistrationParameters(serverProperty, pubKeyCredParams, true, true)
            );

            var aobj      = regData.getAttestationObject();
            var authData  = aobj.getAuthenticatorData();
            AttestedCredentialData attested = authData.getAttestedCredentialData();

            // Serializaciones recomendadas
            var cbor    = objectConverter.getCborConverter();
            var json    = objectConverter.getJsonConverter();
            var acdConv = new AttestedCredentialDataConverter(objectConverter);

            byte[] attestedBytes = acdConv.convert(attested);
            byte[] attStmtEnv    = cbor.writeValueAsBytes(new AttestationStatementEnvelope(aobj.getAttestationStatement()));
            String clientExtJson = json.writeValueAsString(regData.getClientExtensions());
            String transportsJson= json.writeValueAsString(regData.getTransports());

            byte[] attObjCbor = cbor.writeValueAsBytes(regData.getAttestationObject());
            String ccdJson    = json.writeValueAsString(regData.getCollectedClientData());


            // AAGUID a bytes (robusto en distintas versiones)
            Object aaguidObj   = attested.getAaguid();
            byte[] aaguidBytes = aaguidToBytes(aaguidObj);

            // Construcción y guardado de la entidad
            var e = WebAuthnCredentialEntity.builder()
                .userId(userId)
                .credentialId(attested.getCredentialId())
                .signCount(authData.getSignCount())
                // (opcionales, si quieres seguir guardándolos)
                .attestedCredentialData(attestedBytes)
                .attestationStatementEnvelope(attStmtEnv)
                .clientExtensionsJson(clientExtJson)
                .authenticatorExtensionsCbor(null)
                .transportsJson(transportsJson)
                .aaguid(aaguidBytes)
                // *** NUEVOS CAMPOS CLAVE ***
                .attestationObjectCbor(attObjCbor)
                .collectedClientDataJson(ccdJson)
                .build();
                repo.save(e);

            session.removeAttribute(REG_CHALLENGE);
            session.removeAttribute(REG_USER_ID);
            return ResponseEntity.ok(Map.of("status","ok"));

        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error","Registro inválido","details",ex.getMessage()));
        }
    }



    /* ------------- helpers ------------- */

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
        return 1L; // TODO: reemplaza por el id real del usuario autenticado
    }

    private static byte[] aaguidToBytes(Object aaguidObj) {
        if (aaguidObj == null) return new byte[16];

        // Caso 1: ya es UUID
        if (aaguidObj instanceof UUID u) {
            return uuidToBytes(u);
        }

        // Caso 2: intentar getValue() por reflexión
        try {
            Method m = aaguidObj.getClass().getMethod("getValue");
            Object val = m.invoke(aaguidObj);
            if (val instanceof byte[] arr) return arr;      // algunas versiones devuelven byte[]
            if (val instanceof UUID u2) return uuidToBytes(u2); // otras devuelven UUID
        } catch (Exception ignore) {
            // seguimos al Caso 3
        }

        // Caso 3: intentar parsear toString() como UUID
        try {
            return uuidToBytes(UUID.fromString(aaguidObj.toString()));
        } catch (Exception ignore) {
            // Último recurso: 16 bytes en cero
            return new byte[16];
        }
    }

    private static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }



}
