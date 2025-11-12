package com.gscorp.dv1.incidents.application;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gscorp.dv1.incidents.infrastructure.Incident;
import com.gscorp.dv1.incidents.infrastructure.IncidentRepository;
import com.gscorp.dv1.incidents.web.dto.CreateIncidentRequest;
import com.gscorp.dv1.incidents.web.dto.IncidentDto;
import com.gscorp.dv1.sites.application.SiteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {
    
  private final IncidentRepository repo;
  private final SiteService siteService;

  @Value("${file.incidents_files-dir}")
  private String uploadFilesDir;

  @Transactional(readOnly = true)
  public List<IncidentRepository.DayCount> byDayForClient(Long clientId, LocalDate from, LocalDate to) {
    return repo.byDayForClient(clientId, from, to);
  }

  @Override
  @Transactional(readOnly = true)
  public List<IncidentDto> findAll() {
    return repo.findAll().stream()
        .map(IncidentDto::fromEntity)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public IncidentDto findById(Long id) {
    return repo.findById(id).map(IncidentDto::fromEntity).orElse(null);
  }

  @Override
  @Transactional
  public IncidentDto createIncident(CreateIncidentRequest request) {

    String photoPath = null;
    try {

      MultipartFile photo = request.getPhotoPath();
      if(photo != null && !photo.isEmpty()) {
        // Generar el nombre del archivo
        String originalFilename = photo.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
          fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        String storedFilename = UUID.randomUUID().toString() + fileExtension;

        //Directorio fisico donde se guardara el archivo
        File dest = new File(uploadFilesDir, "photos");
        if(!dest.exists()) dest.mkdirs();
        File storedFile = new File(dest, storedFilename);
        photo.transferTo(storedFile);
        photoPath = "/files/incidents_files/photos/" + storedFilename;

      }
    } catch (Exception e) {
      throw new RuntimeException("Error al guardar archivo", e);
    }

    //Buscar sitio
    var site = siteService.findById(request.getSiteId())
                            .orElseThrow(() -> new RuntimeException("Sitio no encontrado")) ;
    
    //Construir entidad
    var incident = Incident.builder()
                    .site(site)
                    .incidentType(request.getIncidentType())
                    .priority(request.getPriority())
                    .description(request.getDescription())
                    .photoPath(photoPath)
                    .build();

    Incident savedEntity = repo.save(incident);
      
    return IncidentDto.fromEntity(savedEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<IncidentDto> findAllForClients(List<Long> clientIds) {
    return repo.findAllForClients(clientIds);
  }

}