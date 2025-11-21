package com.gscorp.dv1.users.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.users.web.dto.UserUpdateDto;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u")
    List<User> findAllUsers();

    @EntityGraph(attributePaths = {"roles", "clients"})
    Optional<User> findWithRolesAndClientsById(Long id);

    @EntityGraph(attributePaths = {"roles", "clients"})
    @Query("select u from User u")
    List<User> findAllWithRolesAndClients();

    Optional<User> findByInvitationToken(String token);

    // NUEVO: devolver solo los IDs de los clients asociados al usuario
    @Query("SELECT c.id FROM User u JOIN u.clients c WHERE u.id = :userId")
    List<Long> findClientIdsByUserId(@Param("userId") Long userId);

    Optional<User> updateUser(Long userId, UserUpdateDto dto);

}
