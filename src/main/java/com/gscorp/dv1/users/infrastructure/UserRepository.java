package com.gscorp.dv1.users.infrastructure;

import java.util.List;
import java.util.Optional;

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

    @EntityGraph(attributePaths = "clients")
    Optional<User> findWithClientsById(Long id);

}
