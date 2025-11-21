package com.gscorp.dv1.projects.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.projects.web.dto.ProjectSelectDto;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>{
    
    @EntityGraph(attributePaths = {"client", "employees"})
    Optional<Project> findById(Long Id);

    @EntityGraph(attributePaths = {"client", "employees"})
    List<Project> findAll();

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.employees WHERE p.id IN :ids")
    List<Project> findAllByIdWithEmployees(@Param("ids") Set<Long> ids);

    /**
     * Devuelve DTOs (id, name) de los proyectos asociados a un clientId y que estén activos,
     * ordenados por nombre. Ajusta nombres de columnas/relación si tu entidad tiene otra estructura.
     */
    @Query("select new com.gscorp.dv1.projects.web.dto.ProjectSelectDto(p.id, p.name) " +
           "from Project p where p.client.id = :clientId and (p.active = true or p.active is null) order by p.name")
    List<ProjectSelectDto> findDtoByClientId(@Param("clientId") Long clientId);

}
