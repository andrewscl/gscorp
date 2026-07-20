package com.gscorp.dv1.hr.employeeterminations.application;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.admin.clients.application.ClientService;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.enums.EmployeeTransitionStatus;
import com.gscorp.dv1.hr.employeedocs.infrastructure.HumanResourcesDocument;
import com.gscorp.dv1.hr.employeedocs.infrastructure.HumanResourcesDocumentRepository;
import com.gscorp.dv1.hr.employees.infrastructure.Employee;
import com.gscorp.dv1.hr.employees.infrastructure.EmployeeRepository;
import com.gscorp.dv1.hr.employeeterminations.infrastructure.EmployeeTermination;
import com.gscorp.dv1.hr.employeeterminations.infrastructure.EmployeeTerminationRepository;
import com.gscorp.dv1.hr.employeeterminations.infrastructure.projections.EmployeeTerminationProjection;
import com.gscorp.dv1.hr.employeeterminations.web.dto.CreateEmployeeTermination;
import com.gscorp.dv1.hr.employeeterminations.web.dto.EmployeeTerminationDto;
import com.gscorp.dv1.shared.FileStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeTerminationServiceImpl 
                implements EmployeeTerminationService {

    private final ClientService clientService;
    private final EmployeeRepository employeeRepository;
    private final EmployeeTerminationRepository repo;
    private final FileStorageService fileStorageService;
    private final HumanResourcesDocumentRepository employeeDocumentRepository;

    @Value("${file.hr-termmination-files-dir}")
    private String physicalTargetDir;

    @Transactional(readOnly = true)
    public Page<EmployeeTerminationDto> getTransitionRequestTable(
                UUID userExternalId,
                EmployeeTransitionStatus status,
                int page,
                int size) {

        List<Long> clientIds = clientService.getClientIdsByUserExternalId(userExternalId);
        if (clientIds == null || clientIds.isEmpty()) {
            return Page.empty();
        }                    

        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(5, size), 200);
        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<EmployeeTerminationProjection> projections =
                    repo.findByClientIds(clientIds, status, pageable);

        return projections.map(EmployeeTerminationDto::fromProjection);
    }

    @Transactional
    public EmployeeTerminationDto createEmployeeTermination (
                            CreateEmployeeTermination request,
                            SecurityUser securityUser){
        Employee employeeRef = employeeRepository.getReferenceById(request.getEmployeeId());
        HumanResourcesDocument supportingDocument = null;
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            String webPrefixPath = "/files/hr_termination_files/";
            String fileUrl = fileStorageService
                            .storeFile(request.getFile(), physicalTargetDir, webPrefixPath);
            HumanResourcesDocument docEntity =
                                    HumanResourcesDocument.builder()
                                        .employee(employeeRef)
                                        .hrDocumentType(request.getHrDocumentType())
                                        .fileUrl(fileUrl)
                                        .createdBy(securityUser.getUsername())
                                        .updatedBy(null)
                                        .build();
            supportingDocument = employeeDocumentRepository.save(docEntity);
        }
        EmployeeTermination employeeTermination =
                                EmployeeTermination.builder()
                                    .employee(employeeRef)
                                    .supportingDocument(supportingDocument)
                                    .terminationReason(request.getTerminationReason())
                                    .status(EmployeeTransitionStatus.PENDING)
                                    .proposedExitDate(request.getProposedExitDate())
                                    .description(request.getDescription())
                                    .resolvedBy(null)
                                    .resolvedAt(null)
                                    .createdBy(securityUser.getUsername())
                                    .updatedAt(null)
                                    .build();
        repo.save(employeeTermination);
        return EmployeeTerminationDto.fromEntity(employeeTermination);
    }
}
