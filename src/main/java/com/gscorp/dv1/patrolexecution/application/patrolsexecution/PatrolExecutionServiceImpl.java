package com.gscorp.dv1.patrolexecution.application.patrolsexecution;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.employees.web.dto.EmployeeSelectDto;
import com.gscorp.dv1.patrol.infrastructure.patrols.Patrol;
import com.gscorp.dv1.patrolexecution.infrastructure.patrolsexecution.PatrolExecution;
import com.gscorp.dv1.patrolexecution.infrastructure.patrolsexecution.PatrolExecutionRepository;
import com.gscorp.dv1.patrolexecution.web.dto.patrolsexecution.CreatePatrolExecutionRequest;
import com.gscorp.dv1.patrolexecution.web.dto.patrolsexecution.PatrolExecutionDto;
import com.gscorp.dv1.shared.FileStorageService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatrolExecutionServiceImpl implements PatrolExecutionService{
    
    private final FileStorageService fileStorageService;
    private final EmployeeService employeeService;
    private final Patrol patrol;
    private final ZoneResolver zoneResolver;
    private final PatrolExecutionRepository patrolExecutionRepository;

    @PersistenceContext
    private EntityManager em;

    @Value("${file.patrol_files-dir}")
    private String uploadFilesDir;

    @Override
    @Transactional
    public PatrolExecutionDto createPatrolExecution(
                        CreatePatrolExecutionRequest request,
                        Long userId){
        String filePhotoPath = "/files/patrols_files/photos/";
        String fileVideoPath = "/files/patrols_files/videos/";

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

        // Obtener empleado asociado al userId
        EmployeeSelectDto employee = employeeService.findEmployeeByUserId(userId);
        if (employee == null) {
            throw new IllegalStateException("El usuario no tiene un empleado asociado");
        }

        // obtener referencia (proxy) a Employee sin SELECT inmediato
        Employee employeeRef = em.getReference(Employee.class, employee.id());

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
                                        .resolveZone(userId, request.getClientTimeZone());

            // espera getZone() y getSource() en ZoneResolutionResult
            ZoneId resolvedZone = zr.zoneId();
            patrolDateTime = ZonedDateTime.now(resolvedZone).toOffsetDateTime();
            clientTimezoneToStore = resolvedZone.getId();
            timezoneSourceToStore = zr.source();
        }

        //Construir entidad
        var entity = PatrolExecution.builder()
            .patrol(patrol)
            .userId(userId)
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
