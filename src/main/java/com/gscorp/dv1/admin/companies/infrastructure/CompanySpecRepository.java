package com.gscorp.dv1.admin.companies.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanySpecRepository
            extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company>{
    
}
