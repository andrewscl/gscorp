package com.gscorp.dv1.configuration.hrdocuments.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.configuration.hrdocuments.infrastructure.HrDocumentType;
import com.gscorp.dv1.configuration.hrdocuments.infrastructure.HrDocumentTypeRepository;
import com.gscorp.dv1.configuration.hrdocuments.infrastructure.projections.HrDocumentTypeProjection;
import com.gscorp.dv1.configuration.hrdocuments.web.dto.CreateHrDocumentType;
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

    @Transactional
    public HrDocumentTypeDto createHrDocumentType(
                            CreateHrDocumentType request,
                            SecurityUser securityUser){

        HrDocumentType saved = HrDocumentType.builder()
                                .name(request.name())
                                .required(true)
                                .status(request.status())
                                .targetProcess(request.targetProcess())
                                .createdBy(securityUser.getUsername())
                                .updatedBy(null)
                                .build();
        hrDocumentTypeRepository.save(saved);

        return HrDocumentTypeDto.fromEntity(saved);
    }

}
