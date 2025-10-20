package com.gscorp.dv1.employees.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>{
    
    @EntityGraph(attributePaths = {"projects", "user"})
    Optional<Employee> findById(Long id);
}
