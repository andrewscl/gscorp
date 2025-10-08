package com.gscorp.dv1.clients.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepo extends JpaRepository<Client, Long>{

    List<Client> findByActiveTrueOrderByNameAsc();
    
}
