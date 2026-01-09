package com.gscorp.dv1.users.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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


    Optional<User> findByPhone(String phone);


    @Query("SELECT u.employee.id FROM User u WHERE u.id = :userId")
    Optional<Long> findEmployeeIdByUserId(@Param("userId") Long userId);


    @Query(
        value = """
            SELECT DISTINCT
                u.id AS id,
                u.username AS username,
                u.mail AS mail,
                u.phone AS phone,
                u.active AS active,
                e.id AS employeeId
            FROM User u
            LEFT JOIN u.employee e
            WHERE LOWER(u.username) LIKE LOWER(CONCAT('%',:q,'%'))
                OR LOWER(u.mail) LIKE LOWER(CONCAT('%',:q,'%'))
            """
    )
    Page<UserTableProjection> findTableRows(
        @Param("q") String q,
        Pageable pageable
    );

    

}
