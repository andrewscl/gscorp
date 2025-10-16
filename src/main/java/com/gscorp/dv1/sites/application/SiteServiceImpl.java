package com.gscorp.dv1.sites.application;

import java.util.List;

import org.hibernate.annotations.DialectOverride.OverridesAnnotation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.projects.infrastructure.Project;
import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.sites.infrastructure.SiteRepository;
import com.gscorp.dv1.sites.web.dto.SiteDto;
import com.gscorp.dv1.sites.web.dto.UpdateLatLon;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService{

    private final SiteRepository siteRepository;

    @Override
    @Transactional
    public Site saveSite (Site site){
        return siteRepository.save(site);
    }

    @Override
    @Transactional(readOnly = true)
    public Site findById(Long id){
        return siteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Site no encontrado"));
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
    public Site updateSite(Long id, SiteDto siteDto) {
        Site site = siteRepository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("No existe el sitio con id " + id));

        site.setName(siteDto.name());
        site.setAddress(siteDto.address());
        site.setTimeZone(siteDto.timeZone());
        site.setActive(Boolean.TRUE.equals(siteDto.active()));

        /* Si permites cambiar el proyecto asociado:
        if (siteDto.projectId() != null) {
            Project project = projectRepo.findById(siteDto.projectId()).orElse(null);
            site.setProject(project);
        }*/

        // Si quieres permitir cambiar projectName desde el DTO, normalmente NO lo haces porque es redundante.
        // site.setProjectName(siteDto.projectName()); // Solo si tu entidad Site tiene ese campo y realmente lo necesitas.

        return siteRepository.save(site);
    }

}
