package com.gscorp.dv1.clients.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>{

    List<Client> findByActiveTrueOrderByNameAsc();

    @EntityGraph(attributePaths = "users")
    Optional<Client> findById (Long id);
    
    @Query("select c.id from Client c join c.users u where u.id = :userId")
    List<Long> findClientIdsByUserId(Long userId);
    
}
