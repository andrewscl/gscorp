package com.gscorp.dv1.roles.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRole(String role);

    @EntityGraph(attributePaths = "users")
    Optional<Role> findWithUsersById (Long id);

    List<RoleSelectProjection> findAllProjections();

}
