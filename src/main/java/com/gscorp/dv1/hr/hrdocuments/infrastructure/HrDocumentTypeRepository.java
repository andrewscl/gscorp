package com.gscorp.dv1.hr.hrdocuments.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HrDocumentTypeRepository 
                    extends JpaRepository <HrDocumentType, Long> {
    
}
