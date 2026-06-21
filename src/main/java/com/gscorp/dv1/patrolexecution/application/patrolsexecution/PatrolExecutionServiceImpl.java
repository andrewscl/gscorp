package com.gscorp.dv1.patrolexecution.application.patrolsexecution;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.employees.web.dto.EmployeeSelectDto;
import com.gscorp.dv1.enums.PatrolExecutionStatus;
import com.gscorp.dv1.patrol.application.schedules.PatrolScheduleService;
import com.gscorp.dv1.patrol.infrastructure.patrols.Patrol;
import com.gscorp.dv1.patrol.infrastructure.patrols.PatrolRepository;
import com.gscorp.dv1.patrol.infrastructure.schedules.PatrolSchedule;
import com.gscorp.dv1.patrol.web.schedules.dto.PatrolScheduleDto;
import com.gscorp.dv1.patrolexecution.infrastructure.patrolsexecution.PatrolExecution;
import com.gscorp.dv1.patrolexecution.infrastructure.patrolsexecution.PatrolExecutionRepository;
import com.gscorp.dv1.patrolexecution.web.dto.patrolsexecution.CreatePatrolExecutionRequest;
import com.gscorp.dv1.patrolexecution.web.dto.patrolsexecution.PatrolExecutionDto;
import com.gscorp.dv1.patrolexecution.web.dto.patrolsexecution.StartPatrolExecutionRequest;
import com.gscorp.dv1.shared.FileStorageService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatrolExecutionServiceImpl implements PatrolExecutionService{
    
    private final FileStorageService fileStorageService;
    private final EmployeeService employeeService;
    private final PatrolRepository patrolRepository;
    private final ZoneResolver zoneResolver;
    private final PatrolExecutionRepository patrolExecutionRepository;
    private final PatrolScheduleService patrolScheduleService;

    @PersistenceContext
    private EntityManager em;

    @Value("${file.patrol_files-dir}")
    private String uploadFilesDir;


    @Transactional
    public PatrolExecutionDto startPatrolExecution (
                    StartPatrolExecutionRequest request,
                    UUID patrolScheduleExternalId,
                    UUID userExternalId ) {

        PatrolScheduleDto schedule = 
            patrolScheduleService
                .getPatrolExternalIdByScheduleExternalId(patrolScheduleExternalId);

        EmployeeSelectDto employee = employeeService.findEmployeeByUserExternalId(userExternalId);
        if (employee == null) {
            throw new IllegalStateException("El usuario no tiene un empleado asociado");
        }

        Employee employeeRef = em.getReference(Employee.class, employee.id());
        Patrol patrolRef = em.getReference(Patrol.class, schedule.patrolId());
        PatrolSchedule patrolScheduleRef = em.getReference(PatrolSchedule.class, schedule.id());

        var entity = PatrolExecution.builder()
            .patrol(patrolRef)
            .patrolSchedule(patrolScheduleRef)
            .userId(employee.userId())
            .employeeId(employeeRef.getId())
            .description(null)
            .photoPath(null)
            .videoPath(null)
            .latitude(request.latitude())
            .longitude(request.longitude())
            .clientTimezone(request.clientTimezone())
            .timezoneSource(request.timezoneSource())
            .status(PatrolExecutionStatus.IN_PROGRESS)
            .build();

        patrolExecutionRepository.save(entity);

        return PatrolExecutionDto.fromEntity(entity);
    }


    @Transactional
    public PatrolExecutionDto endPatrolExecution(
                        CreatePatrolExecutionRequest request,
                        UUID patrolExternalId,
                        UUID userExternalId) {
        String filePhotoPath = "/files/patrol_files/photos/";
        String fileVideoPath = "/files/patrol_files/videos/";

        String photoPath = null; String videoPath = null;
        try {
            photoPath = fileStorageService.storeFile(
                                                request.getPhoto(),
                                                uploadFilesDir,
                                                filePhotoPath);

            videoPath = fileStorageService.storeFile(
                                                request.getVideo(),
                                                uploadFilesDir,
                                                fileVideoPath);
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar el archivo", e);
        }

        EmployeeSelectDto employee = employeeService.findEmployeeByUserExternalId(userExternalId);
        if (employee == null) {
            throw new IllegalStateException("El usuario no tiene un empleado asociado");
        }

        // obtener referencia (proxy) a Employee sin SELECT inmediato
        Employee employeeRef = em.getReference(Employee.class, employee.id());

        Patrol patrol = patrolRepository.findByExternalId(patrolExternalId)
                                    .orElseThrow(() -> new EntityNotFoundException(
                                        "Failed to retrieve saved patrol with ID: "
                                                + patrolExternalId ));

        OffsetDateTime patrolDateTime = null;
        String clientTimezoneToStore = null;
        String timezoneSourceToStore = null;
        if (request.getPatrolDateTime() != null) {
            // Cliente ya envió un OffsetDateTime (inequívoco)
            patrolDateTime = request.getPatrolDateTime();
            /* Si el cliente también envió clientTimeZone preferirlo,
                si no guardamos el offset como referencia*/
            if (request.getClientTimeZone() != null && !request
                                                            .getClientTimeZone().isBlank()) {
                clientTimezoneToStore = request.getClientTimeZone();
                timezoneSourceToStore = "CLIENT_REQUEST_TZ";
            } else {
                clientTimezoneToStore = patrolDateTime.getOffset().toString(); // e.g. "-03:00"
                timezoneSourceToStore = "CLIENT_PROVIDED_OFFSET";
            }
        } else {
            // No vino OffsetDateTime: resolver zona (requested -> user profile -> system)
            ZoneResolutionResult zr = zoneResolver
                                        .resolveZone(userExternalId, request.getClientTimeZone());

            // espera getZone() y getSource() en ZoneResolutionResult
            ZoneId resolvedZone = zr.zoneId();
            patrolDateTime = ZonedDateTime.now(resolvedZone).toOffsetDateTime();
            clientTimezoneToStore = resolvedZone.getId();
            timezoneSourceToStore = zr.source();
        }

        //Construir entidad
        var entity = PatrolExecution.builder()
            .patrol(patrol)
            .userId(employee.userId())
            .employeeId(employeeRef.getId())
            .description(request.getDescription())
            .photoPath(photoPath)
            .videoPath(videoPath)
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .clientTimezone(clientTimezoneToStore)
            .timezoneSource(timezoneSourceToStore)
            .build();

        PatrolExecution savedPatrolExecution =
                                        patrolExecutionRepository.save(entity);

        return PatrolExecutionDto.fromEntity(savedPatrolExecution);

    }

}
