package com.gscorp.dv1.projects.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>{
    
    @Override
    Optional<Project> findById(Long Id);

    @EntityGraph(attributePaths = {"client", "employees"})
    List<Project> findAllWithClientsAndEmployees();

}
