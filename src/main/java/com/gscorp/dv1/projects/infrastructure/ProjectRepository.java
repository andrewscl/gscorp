package com.gscorp.dv1.projects.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>{
    
    @Override
    public Optional<Project> findById(Long Id);

    @Query("SELECT s FROM Project s JOIN FETCH s.client")
    List<Project> findAllWithClients();

}
