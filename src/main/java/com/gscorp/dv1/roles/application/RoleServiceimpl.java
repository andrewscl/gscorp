package com.gscorp.dv1.roles.application;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.api.dto.RoleDto;
import com.gscorp.dv1.entities.Role;
import com.gscorp.dv1.roles.infrastructure.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceimpl implements RoleService{

    private final RoleRepository roleRepository;

    public void saveRole (Role role){
        role.setRole(role.getRole().toUpperCase());
        roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public List<RoleDto> getAllDtos(){
        return roleRepository.findAll(Sort.by("role").ascending())
                    .stream()
                    .map(r -> new RoleDto(r.getId(), r.getRole()))
                    .toList();
    }

    @Override
    public Role findById(Long id){
        return roleRepository.findById(id)
            .orElseThrow(()-> new IllegalArgumentException("Role no encontrado"));
    }

}
