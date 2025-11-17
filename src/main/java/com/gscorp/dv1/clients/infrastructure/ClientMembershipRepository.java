package com.gscorp.dv1.clients.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientMembershipRepository extends JpaRepository<Client, Long>{

    @Query("select case when count(u) > 0 then true else false end " +
           "from Client c join c.users u " +
           "where c.id = :clientId and u.id = :userId")
    boolean existsByClientIdAndUserId(@Param("clientId") Long clientId,
                                      @Param("userId") Long userId);
    
}
