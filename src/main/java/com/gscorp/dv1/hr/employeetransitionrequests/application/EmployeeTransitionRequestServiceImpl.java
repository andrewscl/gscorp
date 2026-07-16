package com.gscorp.dv1.hr.employeetransitionrequests.application;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.admin.clients.application.ClientService;
import com.gscorp.dv1.enums.EmployeeTransitionRequestStatus;
import com.gscorp.dv1.hr.employeetransitionrequests.infrastructure.EmployeeTransitionRequestRepository;
import com.gscorp.dv1.hr.employeetransitionrequests.infrastructure.projections.EmployeeTransitionRequestProjection;
import com.gscorp.dv1.hr.employeetransitionrequests.web.dto.EmployeeTransitionRequestDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeTransitionRequestServiceImpl 
                implements EmployeeTransitionRequestService {

    private final ClientService clientService;
    private final EmployeeTransitionRequestRepository repo;

    @Transactional(readOnly = true)
    public Page<EmployeeTransitionRequestDto> getTransitionRequestTable(
                UUID userExternalId,
                EmployeeTransitionRequestStatus status,
                int page,
                int size) {

        List<Long> clientIds = clientService.getClientIdsByUserExternalId(userExternalId);
        if (clientIds == null || clientIds.isEmpty()) {
            return Page.empty();
        }                    

        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(5, size), 200);
        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "startDate"));

        Page<EmployeeTransitionRequestProjection> projections =
                    repo.findByClientIds(clientIds, status, pageable);

        return projections.map(EmployeeTransitionRequestDto::fromProjection);
    }

}
