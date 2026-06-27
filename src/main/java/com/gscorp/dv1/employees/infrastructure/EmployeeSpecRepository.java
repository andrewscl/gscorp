package com.gscorp.dv1.employees.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeSpecRepository 
            extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee>{

}
