package com.gscorp.dv1.users.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.enums.UserStatus;
import com.gscorp.dv1.users.infrastructure.projections.UserStatusSummaryProjection;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u")
    List<User> findAllUsers();


    @EntityGraph(attributePaths = {"roles", "clients"})
    Optional<User> findWithRolesAndClientsById(Long id);


    @EntityGraph(attributePaths = {"role", "companies", "clients"})
    @Query("select u from User u")
    List<User> findAllWithCompaniesAndClients();
    
    @Query("""
        SELECT u 
        FROM User u 
        LEFT JOIN FETCH u.companies 
        LEFT JOIN FETCH u.clients 
        WHERE u.externalId = :externalId
    """)
    Optional<User> findWithCompaniesAndClientsByExternalId(@Param("externalId") UUID externalId);


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
                u.status AS status,
                e.id AS employeeId
            FROM User u
            LEFT JOIN u.employee e
            WHERE
                :q IS NULL
                OR LOWER(u.username) LIKE LOWER(CONCAT('%',:q,'%'))
                OR LOWER(u.mail) LIKE LOWER(CONCAT('%',:q,'%'))
            """
    )
    Page<UserTableProjection> findTableRows(
        @Param("q") String q,
        Pageable pageable
    );

    @Query(
        value = """
            SELECT
                u.id AS id,
                u.externalId AS externalId,
                u.username AS username,
                u.mail AS mail,
                u.phone AS phone,
                u.active AS active,
                u.status AS status,
                e.id AS employeeId,
                r.role AS roleName
            FROM User u
            LEFT JOIN u.employee e
            LEFT JOIN u.role r
            """,
        countQuery = "SELECT COUNT(u.id) FROM User u"
    )
    Page<UserTableProjection> findAllUsersWithEmployee(
        Pageable pageable
    );

    Long countByStatus (UserStatus status);

    Optional<User> findByExternalId (UUID externalId);


    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN u.status = 'INVITED' THEN 1 ELSE 0 END), 0L) AS invitedCount,
            COALESCE(SUM(CASE WHEN u.status = 'PENDING' THEN 1 ELSE 0 END), 0L) AS pendingCount,
            COALESCE(SUM(CASE WHEN u.status = 'ACTIVE' THEN 1 ELSE 0 END), 0L) AS activeCount,
            COALESCE(SUM(CASE WHEN u.status = 'INACTIVE' THEN 1 ELSE 0 END), 0L) AS inactiveCount,
            COALESCE(SUM(CASE WHEN u.status = 'EXPIRED' THEN 1 ELSE 0 END), 0L) AS expiredCount,
            COALESCE(SUM(CASE WHEN u.status = 'SUSPENDED' THEN 1 ELSE 0 END), 0L) AS suspendedCount
        FROM User u
    """)
    List<UserStatusSummaryProjection> getUserStatusSummary();


}
