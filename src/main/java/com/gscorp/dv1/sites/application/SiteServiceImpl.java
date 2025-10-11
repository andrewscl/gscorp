package com.gscorp.dv1.sites.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.sites.infrastructure.SiteRepository;
import com.gscorp.dv1.sites.web.dto.SiteDto;

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
        return siteRepository.findAll()
                    .stream()
                    .map(r-> new SiteDto(r.getId(), r.getClient().getId(),r.getName(), r.getCode(), r.getAddress()))
                    .toList();
    }

}
