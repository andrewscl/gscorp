package com.gscorp.dv1.users.web;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.web.dto.CreateUserRequest;
import com.gscorp.dv1.users.web.dto.UserDto;
import com.gscorp.dv1.users.web.dto.UserUpdateDto;
import com.gscorp.dv1.users.web.dto.UserViewDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class UserRestController {

        private final UserService userService;

        @PostMapping("/create")
        public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {
            Long id = userService.createUser(req);
            return ResponseEntity
                    .created(URI.create("/api/users/" + id)).build();
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(@PathVariable Long id) {
                userService.deleteById(id);
                return ResponseEntity.noContent().build();
        }

        @GetMapping("/{id}")
        public ResponseEntity<UserDto> getById(@PathVariable Long id) {
                User user = userService.findById(id);
                return ResponseEntity.ok(UserDto.fromEntity(user));
        }

        @PatchMapping("/{id}")
        public ResponseEntity<?> patchUser(@PathVariable("id") Long id, @RequestBody JsonNode body) {

        if (id == null) {
            return ResponseEntity.badRequest().body(error("userId requerido"));
        }

        if (body == null || body.isNull()) {
            return ResponseEntity.badRequest().body(error("body requerido"));
        }

        try {
            UserUpdateDto dto = buildDtoFromJson(body);

            Optional<User> updated = userService.updateUser(id, dto);
            if (updated.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("Usuario no encontrado"));
            }

            // Mapear a una vista segura para devolver (no exponer password, etc.)
            UserViewDto view = UserViewDto.from(updated.get());
            return ResponseEntity.ok(view);

        } catch (IllegalArgumentException ex) {
            log.debug("Bad request updating user {}: {}", id, ex.getMessage());
            return ResponseEntity.badRequest().body(error(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Error actualizando usuario " + id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error("Error interno actualizando usuario"));
        }
    }

    private UserUpdateDto buildDtoFromJson(JsonNode body) {
        // username
        String username = null;
        if (body.has("username")) {
            JsonNode n = body.get("username");
            username = n.isNull() ? null : n.asText().trim();
        }

        // mail
        String mail = null;
        if (body.has("mail")) {
            JsonNode n = body.get("mail");
            mail = n.isNull() ? null : n.asText().trim();
        }

        // active (Boolean)
        Boolean active = null;
        if (body.has("active")) {
            JsonNode n = body.get("active");
            active = n.isNull() ? null : n.asBoolean();
        }

        // roleIds (array of numbers)
        Set<Long> roleIds = null;
        if (body.has("roleIds")) {
            JsonNode arr = body.get("roleIds");
            roleIds = parseLongSetFromArrayNode(arr);
        }

        // clientIds
        Set<Long> clientIds = null;
        if (body.has("clientIds")) {
            JsonNode arr = body.get("clientIds");
            clientIds = parseLongSetFromArrayNode(arr);
        }

        // employeeId: if present and null => send null (service will interpret as "clear" or no-change per policy)
        Long employeeId = null;
        if (body.has("employeeId")) {
            JsonNode n = body.get("employeeId");
            if (n.isNull()) {
                employeeId = null;
            } else if (n.canConvertToLong()) {
                employeeId = n.asLong();
            } else if (n.isTextual() && !n.asText().isBlank()) {
                try {
                    employeeId = Long.parseLong(n.asText().trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("employeeId inválido");
                }
            }
        }

        // timeZone
        String timeZone = null;
        if (body.has("timeZone")) {
            JsonNode n = body.get("timeZone");
            timeZone = n.isNull() ? null : n.asText().trim();
        }

        return new UserUpdateDto(
                username,
                mail,
                active,
                roleIds,
                clientIds,
                employeeId,
                timeZone
        );
    }

    private Set<Long> parseLongSetFromArrayNode(JsonNode arr) {
        if (arr == null || arr.isNull()) return null;
        if (!arr.isArray()) throw new IllegalArgumentException("se esperaba un array de ids");
        Set<Long> out = new HashSet<>();
        Iterator<JsonNode> it = arr.elements();
        while (it.hasNext()) {
            JsonNode n = it.next();
            if (n == null || n.isNull()) continue;
            if (n.canConvertToLong()) {
                out.add(n.asLong());
            } else if (n.isTextual() && !n.asText().isBlank()) {
                try {
                    out.add(Long.valueOf(n.asText().trim()));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("id inválido en array");
                }
            }
        }
        return out;
    }

    private static Map<String, String> error(String msg) {
        return Collections.singletonMap("message", msg);
    }


}
