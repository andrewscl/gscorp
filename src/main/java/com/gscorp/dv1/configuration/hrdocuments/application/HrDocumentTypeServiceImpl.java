package com.gscorp.dv1.configuration.hrdocuments.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.configuration.hrdocuments.infrastructure.HrDocumentTypeRepository;
import com.gscorp.dv1.configuration.hrdocuments.infrastructure.projections.HrDocumentTypeProjection;
import com.gscorp.dv1.configuration.hrdocuments.web.dto.HrDocumentTypeDto;
import com.gscorp.dv1.enums.EmployeeStatus;
import com.gscorp.dv1.enums.HrProcessType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HrDocumentTypeServiceImpl implements HrDocumentTypeService{

    private final HrDocumentTypeRepository hrDocumentTypeRepository;

    @Transactional(readOnly = true)
    public Page<HrDocumentTypeDto> findByStatusAndProcess (
                            EmployeeStatus status,
                            HrProcessType process,
                            Pageable pageable){

        Page<HrDocumentTypeProjection> projections = 
            hrDocumentTypeRepository
                            .findByStatusAndProcess(status, process, pageable);
        return projections.map(HrDocumentTypeDto::fromProjection);
    }

}
