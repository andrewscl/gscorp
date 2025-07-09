package com.gscorp.dv1.services;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.repositories.RoleRepository;
import com.gscorp.dv1.entities.Role;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public void saveRole (Role role){
        role.setRole(role.getRole().toUpperCase());
        roleRepository.save(role);
    }

}
