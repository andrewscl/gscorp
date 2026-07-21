package com.gscorp.dv1.hr.employeedocs.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HumanResourcesDocumentRepository
                    extends JpaRepository <HumanResourcesDocument, Long> {

    
    
}
