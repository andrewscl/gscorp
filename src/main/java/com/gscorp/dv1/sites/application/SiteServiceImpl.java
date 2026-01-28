package com.gscorp.dv1.sites.application;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.clients.application.ClientService;
import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.sites.infrastructure.SiteProjection;
import com.gscorp.dv1.sites.infrastructure.SiteSelectProjection;
import com.gscorp.dv1.sites.infrastructure.SiteRepository;
import com.gscorp.dv1.sites.web.dto.SetSiteCoordinatesDto;
import com.gscorp.dv1.sites.web.dto.SiteDto;
import com.gscorp.dv1.sites.web.dto.SiteDtoProjection;
import com.gscorp.dv1.sites.web.dto.SiteSelectDto;
import com.gscorp.dv1.sites.web.dto.UpdateLatLon;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService{

    private final SiteRepository siteRepository;
    private final ClientService clientService;
    private final UserService userService;

    @Override
    @Transactional
    public Site saveSite (Site site){
        return siteRepository.save(site);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Site> findById(Long id){
        return siteRepository.findById(id);
    }

    @Override
    public List<SiteDto>getAllSites(){
        return siteRepository.findAllWithProjects()
                    .stream()
                    .map(r-> new SiteDto(
                                    r.getId(),
                                    r.getProject().getId(),
                                    r.getProject().getName(),
                                    r.getName(),
                                    r.getAddress(),
                                    r.getTimeZone(),
                                    r.getLat(),
                                    r.getLon(),
                                    r.getActive()))
                    .toList();
    }

    //Eliminar sitio
    @Override
    @Transactional
    public void deleteById(Long id){
        if(!siteRepository.existsById(id)){
            throw new IllegalArgumentException("Site no encontrado");
        }
        try{
            siteRepository.deleteById(id);
        } catch (DataIntegrityViolationException e){
            throw new IllegalArgumentException("No se puede eliminar el sitio");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Site findByIdWithProjects(Long id){
        return siteRepository.findById(id)
                .orElseThrow( ()->
                    new IllegalArgumentException("Cliente no encontrado" + id));
    }

    @Override
    public Site updateSiteLocation(Long id, UpdateLatLon updateLatLon) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() ->
                new IllegalArgumentException("Site no encontrado" + id));

        site.setLat(updateLatLon.lat());
        site.setLon(updateLatLon.lon());
        return siteRepository.save(site);
    }

    @Override
    @Transactional
    public SiteDto updateSite(Long id, SiteDto siteDto) {
        Site site = siteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("No existe el sitio con id " + id));

        site.setName(siteDto.name());
        site.setAddress(siteDto.address());
        site.setTimeZone(siteDto.timeZone());
        site.setLat(siteDto.lat());
        site.setLon(siteDto.lon());
        site.setActive(Boolean.TRUE.equals(siteDto.active()));


        // Si permites cambiar el proyecto asociado:
        // if (siteDto.projectId() != null) {
        //     Project project = projectRepo.findById(siteDto.projectId()).orElse(null);
        //     site.setProject(project);
        // }

        siteRepository.save(site);

        // Forzar inicialización mientras la sesión está activa
        Long projectId = site.getProject() != null ? site.getProject().getId() : null;
        String projectName = site.getProject() != null ? site.getProject().getName() : null;

        return new SiteDto(
            site.getId(),
            projectId,
            projectName,
            site.getName(),
            site.getAddress(),
            site.getTimeZone(),
            site.getLat(),
            site.getLon(),
            site.getActive()
        );
    }

    @Override
    @Transactional
    public SetSiteCoordinatesDto setCoordinates(Long siteId, Double latitude, Double longitude) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site no encontrado: " + siteId));
        site.setLat(latitude);
        site.setLon(longitude);
        siteRepository.save(site);
        return new SetSiteCoordinatesDto(site.getId(), site.getLat(), site.getLon());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SiteSelectDto> getAllSitesForClients(List<Long> clientIds) {
        return siteRepository.findByProject_Client_IdIn(clientIds)
            .stream()
            .map(site -> new SiteSelectDto(site.
                                getId(), site.getName(), site.getLat(), site.getLon()))
            .toList();
    }

    // Nuevo método recomendado: devolver clientId del site (útil para validaciones rápidas)
    @Override
    @Transactional(readOnly = true)
    public Optional<Long> getClientIdForSite(Long siteId) {
        Optional<Long> clientId = siteRepository.findClientIdBySiteId(siteId);
        return clientId;
    }


    @Override
    public List<SiteSelectDto> findSelectDtoByProjectId(Long projectId) {
        if (projectId == null) return List.of();
        return siteRepository.findSelectDtoByProjectId(projectId);
    }


    @Override
    public List<SiteSelectDto> findByUserId(Long userId) {

        List<Long> clientIds = clientService.getClientIdsByUserId(userId);
        if(clientIds == null || clientIds.isEmpty()) {
            return null;
        }

        List<SiteSelectProjection> sites = siteRepository.findByClientIds(clientIds);
        if(sites == null || sites.isEmpty()) {
            return null;
        }

        List<SiteSelectDto> response = sites.stream()
            .map(s -> new SiteSelectDto(s.getId(), s.getName(), s.getLat(), s.getLon()))
            .toList();
        
        return response;
    }


    @Override
    public SiteSelectDto findNearestSite(Long userId, double lat, double lon) {

        List<Long> clientIds = clientService.getClientIdsByUserId(userId);
        log.debug("findNearestSite: userId={}, clientIds={}", userId, clientIds);
        if(clientIds == null || clientIds.isEmpty()) {
            return null;
        }

        List<SiteSelectProjection> sites = siteRepository.findByClientIds(clientIds);
        log.debug("findNearestSite: sites fetched={}, for userId={}", sites == null ? 0 : sites.size(), userId);
        if(sites == null || sites.isEmpty()) {
            return null;
        }

        // Filtrar sites que tengan lat/lon válidos para evitar NPE en la comparación
        Optional<SiteSelectProjection> nearest = sites.stream()
            .filter(s -> s.getLat() != null && s.getLon() != null)
            .min(Comparator.comparingDouble(
                        s -> haversineMeters(lat, lon, s.getLat(), s.getLon())));

        if (nearest.isEmpty()) {
                log.debug("findNearestSite: after filtering, no sites with lat/lon for clientIds {}", clientIds);
            return null;
        }

        SiteSelectProjection p = nearest.get();

        // Mapear la proyección a tu DTO de salida. Ajusta constructor/nombres
        // según tu DTO real.
        return new SiteSelectDto(p.getId(), p.getName(), p.getLat(), p.getLon());

    }


    /** Utilidad geodésica */
    @Override
    @Transactional(readOnly = true)
    public double haversineMeters(double lat1,double lon1,double lat2,double lon2){
        double R=6371000, dLat=Math.toRadians(lat2-lat1), dLon=Math.toRadians(lon2-lon1);
        double a=Math.sin(dLat/2)*Math.sin(dLat/2)
               + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
               * Math.sin(dLon/2)*Math.sin(dLon/2);
    return 2*R*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
    }


    @Override
    @Transactional(readOnly = true)
    public List<SiteDtoProjection> findSiteProjectionsByClientIds(List<Long> clientIds) {

        if (clientIds == null || clientIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<SiteProjection>  siteProjections = siteRepository.findSiteProjectionsByClientIds(clientIds);

        // Mapea cada SiteProjection a SiteDtoProjection usando el método fromEntity
        List<SiteDtoProjection> dtolist = siteProjections.stream()
            .map(SiteDtoProjection::fromProjection) // Convierte cada proyección usando el método
            .toList(); // Convierte el stream en una lista
        
        return dtolist;

    }


    @Override
    @Transactional(readOnly = true)
    public List<SiteDtoProjection> findSiteProjectionsByUserId(Long userId) {

        List<Long> clientIds = userService.getClientIdsForUser(userId);
        if (clientIds == null || clientIds.isEmpty()) {
            throw new IllegalArgumentException(
                "User with ID " + userId + " is not associated with any clients."
            );
        }

        List<SiteProjection> siteProjections = siteRepository.findSiteProjectionsByClientIds(clientIds);

        List<SiteDtoProjection> dtolist = siteProjections.stream()
            .map(SiteDtoProjection::fromProjection)
            .toList();

        return dtolist;
    }


}
