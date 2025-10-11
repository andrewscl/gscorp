package com.gscorp.dv1.clients.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepo extends JpaRepository<Client, Long>{

    List<Client> findByActiveTrueOrderByNameAsc();

    @EntityGraph(attributePaths = "users")
    Optional<Client> findByIdWithUsers (Long id);
    
}
