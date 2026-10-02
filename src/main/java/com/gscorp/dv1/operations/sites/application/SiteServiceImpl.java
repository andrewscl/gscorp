package com.gscorp.dv1.operations.sites.application;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.admin.clients.application.ClientService;
import com.gscorp.dv1.exceptions.ResourceNotFoundException;
import com.gscorp.dv1.operations.sites.infrastructure.Site;
import com.gscorp.dv1.operations.sites.infrastructure.SiteProjection;
import com.gscorp.dv1.operations.sites.infrastructure.SiteRepository;
import com.gscorp.dv1.operations.sites.web.dto.SetSiteCoordinatesDto;
import com.gscorp.dv1.operations.sites.web.dto.SiteDto;
import com.gscorp.dv1.operations.sites.web.dto.UpdateLatLon;
import com.gscorp.dv1.operations.sites.web.dto.UpdateSiteRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService{

    private final SiteRepository siteRepository;
    private final ClientService clientService;

    @Transactional
    public Site saveSite (Site site){
        return siteRepository.save(site);
    }

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

    @Transactional(readOnly = true)
    public Optional<Site> findById(Long id){
        return siteRepository.findById(id);
    }

    @Transactional (readOnly = true)
    public Optional<Site> findByExternalId(
                            boolean ignoreProjectFilter,
                            List<Long> projectIds,
                            UUID externalId){
        return siteRepository.findByExternalId(
                                ignoreProjectFilter,
                                projectIds,
                                externalId);
    }

    @Transactional(readOnly = true)
    public List<SiteDto> getAllSites(){
        return siteRepository.findAllWithProjects()
                    .stream()
                    .map(r-> new SiteDto(
                                    r.getId(),
                                    r.getExternalId(),
                                    r.getProject().getId(),
                                    r.getProject().getName(),
                                    r.getName(),
                                    r.getAddress(),
                                    r.getTimeZone(),
                                    r.getLat(),
                                    r.getLon(),
                                    r.getStatus(),
                                    r.getActive()))
                    .toList();
    }

    @Transactional(readOnly = true)
    public List<SiteDto> getAllSitesByUser(UUID userExternalId) {
        List<Long> clientIds = clientService.getClientIdsByUserExternalId(userExternalId);
        if(clientIds == null || clientIds.isEmpty()) {
            return Collections.emptyList();
            }
        return siteRepository.findByProject_Client_IdIn(clientIds)
            .stream()
            .map(SiteDto::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public Site findByIdWithProjects(Long id){
        return siteRepository.findById(id)
                .orElseThrow( ()->
                    new IllegalArgumentException("Cliente no encontrado" + id));
    }

    @Transactional
    public Site updateSiteLocation(Long id, UpdateLatLon updateLatLon) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() ->
                new IllegalArgumentException("Site no encontrado" + id));

        site.setLat(updateLatLon.lat());
        site.setLon(updateLatLon.lon());
        return siteRepository.save(site);
    }

    @Transactional
    public SiteDto updateSite(
                    boolean ignoreProjectFilter,
                    List<Long> projectIds,
                    UUID externalId,
                    UpdateSiteRequest request) {
        Site site = siteRepository.findByExternalId(
                                    ignoreProjectFilter,
                                    projectIds,
                                    externalId)
            .orElseThrow(() -> new RuntimeException("No existe el sitio con id " + externalId));
        site.setName(request.name());
        site.setAddress(request.address());
        site.setTimeZone(request.timeZone());
        site.setLat(request.lat());
        site.setLon(request.lon());
        site.setStatus(request.status());
        site.setActive(Boolean.TRUE.equals(request.active()));
        siteRepository.save(site);
        // Forzar inicialización mientras la sesión está activa
        Long projectId = site.getProject() != null ? site.getProject().getId() : null;
        String projectName = site.getProject() != null ? site.getProject().getName() : null;
        return new SiteDto(
            site.getId(),
            site.getExternalId(),
            projectId,
            projectName,
            site.getName(),
            site.getAddress(),
            site.getTimeZone(),
            site.getLat(),
            site.getLon(),
            site.getStatus(),
            site.getActive()
        );
    }

    @Transactional
    public SetSiteCoordinatesDto setCoordinates(Long siteId, Double latitude, Double longitude) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site no encontrado: " + siteId));
        site.setLat(latitude);
        site.setLon(longitude);
        siteRepository.save(site);
        return new SetSiteCoordinatesDto(site.getId(), site.getLat(), site.getLon());
    }

    @Transactional(readOnly = true)
    public List<SiteDto> getAllSitesForClients(List<Long> clientIds) {
        return siteRepository.findByProject_Client_IdIn(clientIds)
            .stream()
            .map(SiteDto::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Long> getClientIdForSite(Long siteId) {
        Optional<Long> clientId = siteRepository.findClientIdBySiteId(siteId);
        return clientId;
    }

    @Transactional(readOnly = true)
    public List<SiteDto> findDtosByProjectId(Long projectId) {
        if (projectId == null) return List.of();
        return siteRepository.findDtoByProjectId(projectId);
    }


    @Transactional(readOnly = true)
    public List<SiteDto> findByUserScope(
                                    boolean ignoreProjectFilter,
                                    List<Long> projectIds) {
        return siteRepository.findByUserScope(
                                    ignoreProjectFilter,
                                    projectIds)
                                    .stream()
                                    .map(SiteDto::fromProjection)
                                    .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public SiteDto findNearestSite(
                        boolean ignoreProjectFilter,
                        List<Long> projectIds,
                        double lat,
                        double lon) {
        if (projectIds == null || projectIds.isEmpty()) return null;
        List<SiteProjection> sites = siteRepository.findByProjectIds(projectIds);
        if(sites == null || sites.isEmpty()) {
            return null;
        }
        // Filtrar sites que tengan lat/lon válidos para evitar NPE en la comparación
        Optional<SiteProjection> nearest = sites.stream()
            .filter(s -> s.getLat() != null && s.getLon() != null)
            .min(Comparator.comparingDouble(
                        s -> haversineMeters(lat, lon, s.getLat(), s.getLon())));
        if (nearest.isEmpty()) {
            return null;
        }
        SiteProjection p = nearest.get();
        return SiteDto.fromProjection(p);
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
    public List<SiteDto> findSiteProjectionsByClientIds(List<Long> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<SiteProjection>  siteProjections = siteRepository.findSiteProjectionsByClientIds(clientIds);
        List<SiteDto> dtolist = siteProjections.stream()
            .map(SiteDto::fromProjection)
            .toList();
        return dtolist;
    }


    @Override
    @Transactional(readOnly = true)
    public List<SiteDto> findDtoByUserExternalId(UUID userExternalId) {
        List<Long> clientIds = clientService.getClientIdsByUserExternalId(userExternalId);
        if (clientIds == null || clientIds.isEmpty()) {
            throw new IllegalArgumentException(
                "User with ID " + userExternalId + " is not associated with any clients."
            );
        }
        List<SiteProjection> siteProjections = siteRepository.findSiteProjectionsByClientIds(clientIds);
        List<SiteDto> dtolist = siteProjections.stream()
            .map(SiteDto::fromProjection)
            .toList();
        return dtolist;
    }

    @Override
    @Transactional(readOnly = true)
    public SiteDto findDtoById(Long siteId) {
        SiteProjection siteDtoProjection =
                siteRepository.findProjectionById(siteId)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Site no encontrado con ID: " + siteId)
                );
        return SiteDto.fromProjection(siteDtoProjection);
    }

    @Transactional (readOnly = true)
    public Optional<SiteDto> findDtoByExternalId(
                    boolean ignoreProjectFilter,
                    List<Long> projectIds,
                    UUID externalId) {
        if(externalId == null) return Optional.empty();
        return siteRepository.findProjectionByExternalId(ignoreProjectFilter, projectIds, externalId)
                .map(SiteDto::fromProjection);
    }

    @Transactional (readOnly = true)
    public List<SiteDto> findDtosByProjectExternalId(
                    boolean ignoreProjectFilter,
                    List<Long> projectIds,
                    UUID projectExternalId) {
        if(projectExternalId == null) return Collections.emptyList();
        return siteRepository.findProjectionsByProjectExternalId(
                                        ignoreProjectFilter, projectIds, projectExternalId)
                                        .stream()
                                        .map(SiteDto::fromProjection)
                                        .toList();
    }

}
