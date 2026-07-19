package com.gscorp.dv1.hr.hrdocuments.application;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.enums.EmployeeStatus;
import com.gscorp.dv1.enums.HrProcessType;
import com.gscorp.dv1.hr.hrdocuments.infrastructure.HrDocumentTypeRepository;
import com.gscorp.dv1.hr.hrdocuments.infrastructure.projections.HrDocumentTypeProjection;
import com.gscorp.dv1.hr.hrdocuments.web.dto.HrDocumentTypeDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HrDocumentTypeServiceImpl implements HrDocumentTypeService{

    private final HrDocumentTypeRepository hrDocumentTypeRepository;

    @Transactional(readOnly = true)
    public Page<HrDocumentTypeDto> findByStatusAndProcess (
                            EmployeeStatus status, HrProcessType process){
        PageRequest pageable =
            PageRequest
                .of(0, 200, Sort.by(Sort.Direction.ASC));
        Page<HrDocumentTypeProjection> projections = 
                        hrDocumentTypeRepository
                            .findByStatusAndProcess(status, process, pageable);
        return projections.map(HrDocumentTypeDto::fromProjection);
    }

    @Transactional(readOnly = true)
    public Page<HrDocumentTypeDto> getHrDocumentTypesList(
                                        UUID userExternalId,
                                        EmployeeStatus status,
                                        HrProcessType targetProcess,
                                        int page,
                                        int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(5, size), 200);
        PageRequest pageable =
            PageRequest.of(safePage, safeSize,
                    Sort.by(Sort.Direction.DESC, "startDate"));

        Page<HrDocumentTypeProjection> projections =
                        hrDocumentTypeRepository
                            .findByStatusAndProcess(status, targetProcess, pageable);

        return projections.map(HrDocumentTypeDto::fromProjection);
    }

}
