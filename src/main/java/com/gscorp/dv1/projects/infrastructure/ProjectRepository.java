package com.gscorp.dv1.projects.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>{
    
    @EntityGraph(attributePaths = {"client", "employees"})
    Optional<Project> findById(Long Id);

    @EntityGraph(attributePaths = {"client", "employees"})
    List<Project> findAll();

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.employees WHERE p.id IN :ids")
    List<Project> findAllByIdWithEmployees(@Param("ids") Set<Long> ids);

}
