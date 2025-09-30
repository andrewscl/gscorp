package com.gscorp.dv1.roles.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.roles.infrastructure.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRole(String role);

    @Query("""
            select distinct r
            from Role r
            left join fetch r.users
            where r.id = :id
            """)
    Optional<Role> findByIdWithUsers (@Param("id") Long id);
}
