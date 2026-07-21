package com.gscorp.dv1.hr.hrdocs.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.hr.hrdocs.infrastructure.HumanResourcesDocumentRepository;
import com.gscorp.dv1.hr.hrdocs.web.dto.HumanResourcesDocumentDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HumanResourcesDocumentServiceImpl {

    private final HumanResourcesDocumentRepository humanResourcesDocumentRepository;
    
    @Transactional(readOnly = true)
    public List<HumanResourcesDocumentDto> findByEmployeeTerminationExternalId(
                                                    UUID employeeTerminationExternalId){
        return humanResourcesDocumentRepository
                    .findByEmployeeTerminationExternalId(employeeTerminationExternalId)
                    .stream()
                    .map(HumanResourcesDocumentDto::fromProjection)
                    .toList();
    }

}
