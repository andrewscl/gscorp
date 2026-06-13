package com.gscorp.dv1.professions.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.professions.infrastructure.projections.ProfessionSelectProjection;

@Repository
public interface ProfessionRepository extends JpaRepository<Profession, Long> {

    @Override
    @Transactional(readOnly = true)
    List<Profession> findAllById(Iterable<Long> ids);


    @Query("""
        SELECT 
            p.id as id,
            p.name as name
        FROM Profession p 
        JOIN p.employees e 
        WHERE e.externalId = :employeeExternalId
    """)
    List<ProfessionSelectProjection>
        findProfessionSelectDtosByEmployeeExternalId(
                @Param("employeeExternalId") UUID employeeExternalId);



}
