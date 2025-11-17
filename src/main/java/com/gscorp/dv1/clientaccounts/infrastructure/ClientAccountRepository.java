package com.gscorp.dv1.clientaccounts.infrastructure;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.clientaccounts.web.dto.ClientAccountDto;

@Repository
public interface ClientAccountRepository extends JpaRepository<ClientAccount, Long> {
    
    @Query("select new com.gscorp.dv1.clientaccounts.web.dto.ClientAccountDto(ca.id, ca.name, ca.client.id, ca.client.name, ca.notes) " +
        "from ClientAccount ca where ca.client.id in :clientIds order by ca.name")
    List<ClientAccountDto> findDtoByClientIds(@Param("clientIds") Collection<Long> clientIds);

    /*@Query("select new com.gscorp.dv1.clientaccounts.web.dto.ClientAccountDto(" +
           "  ca.id, ca.name, ca.client.id, ca.client.name, ca.notes) " +
           "from ClientAccount ca " +
           "join ca.client c " +
           "join c.projects p " +
           "join p.sites s " +
           "join c.users u " +
           "where s.id = :siteId and u.id = :userId " +
           "order by ca.name")
    List<ClientAccountDto> findDtoBySiteIdAndUserId(@Param("siteId") Long siteId,
                                                    @Param("userId") Long userId);*/

}
