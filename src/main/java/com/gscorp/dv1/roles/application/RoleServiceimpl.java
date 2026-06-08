package com.gscorp.dv1.roles.application;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.roles.infrastructure.Role;
import com.gscorp.dv1.roles.infrastructure.RoleRepository;
import com.gscorp.dv1.roles.infrastructure.RoleSelectProjection;
import com.gscorp.dv1.roles.web.dto.RoleDto;
import com.gscorp.dv1.roles.web.dto.RoleSelectDto;
import com.gscorp.dv1.roles.web.dto.UpdateRoleRequest;

import jakarta.persistence.EntityNotFoundException;
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
    public List<RoleDto> getAllRoles(){
        return roleRepository.findAll(Sort.by("role").ascending())
                    .stream()
                    .map(r -> new RoleDto(
                                    r.getId(), r.getExternalId().toString(), r.getRole(), r.getAccountType()))
                    .toList();
    }

    @Override
    public Role findById(Long id){
        return roleRepository.findById(id)
            .orElseThrow(()-> new IllegalArgumentException("Role no encontrado"));
    }

    @Override
    public Role findWithUsersById (Long id){
        return roleRepository.findWithUsersById(id)
            .orElseThrow(()-> new IllegalArgumentException("Rol no encontrado" + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleSelectDto> getAllRolesSelectDto() {

        List<RoleSelectProjection> projections = roleRepository.findAllProjections();

        return projections.stream()
                .map(RoleSelectDto::fromProjection)
                .toList();
    }


    @Override
    @Transactional
    public RoleDto patchRole (
                    String externalIdStr, UpdateRoleRequest request) {

        UUID externalId = UUID.fromString(externalIdStr);

        Role role = roleRepository.findByExternalId(externalId)
                .orElseThrow(() ->
                    new EntityNotFoundException(
                            "No role found with external ID: " + externalId)
                );

        if(request.accountType()!=null){
            role.setAccountType(request.accountType());
        }

        roleRepository.save(role);

        return RoleDto.fromEntity(role);
    }

    @Override
    @Transactional
    public RoleDto findByExternalId(String externalIdStr) {
        UUID externalId = UUID.fromString(externalIdStr);
        Role role = roleRepository.findByExternalId(externalId)
                .orElseThrow(() ->
                    new EntityNotFoundException(
                            "No role found with external ID: " + externalId)
                );
        return RoleDto.fromEntity(role);
        
    }

}
