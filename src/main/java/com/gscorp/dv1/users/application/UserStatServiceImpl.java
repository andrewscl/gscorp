package com.gscorp.dv1.users.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.users.infrastructure.UserRepository;
import com.gscorp.dv1.users.infrastructure.projections.statistics.RoleUsersSummaryProjection;
import com.gscorp.dv1.users.infrastructure.projections.statistics.UserStatusSummaryProjection;
import com.gscorp.dv1.users.web.dto.statistics.RoleUsersSummaryDto;
import com.gscorp.dv1.users.web.dto.statistics.UserStatusSummaryDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserStatServiceImpl implements UserStatService{

    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public List<UserStatusSummaryDto> getUsersStatusSummary() {
        List<UserStatusSummaryProjection> projections =
                    userRepository.getUserStatusSummary();
        return projections
                    .stream()
                    .map(UserStatusSummaryDto::fromProjection)
                    .toList();
    }


    @Transactional(readOnly = true)
    public List<RoleUsersSummaryDto> getRoleUsersSummary() {
        List<RoleUsersSummaryProjection> projections =
                    userRepository.getRoleUsersSummary();
        return projections
                    .stream()
                    .map(RoleUsersSummaryDto::fromProjection)
                    .toList();
    }

}
