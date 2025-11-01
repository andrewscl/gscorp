package com.gscorp.dv1.employees.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>{
    
    @EntityGraph(attributePaths = {"projects", "user"})
    Optional<Employee> findById(Long id);
    Optional<Employee> findByUserUsername(String username);

    @Query("SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.projects")
    List<Employee> findAllWithProjects();

    @Query("SELECT e FROM Employee e WHERE e.user IS NULL")
    List<Employee> findAllUnassignedEmployees();

    @EntityGraph(attributePaths = {"user", "projects"})
    @Query("SELECT e FROM Employee e")
    List<Employee> findAllWithUserAndProjects();


}
