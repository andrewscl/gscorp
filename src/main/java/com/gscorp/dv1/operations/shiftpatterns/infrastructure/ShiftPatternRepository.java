package com.gscorp.dv1.operations.shiftpatterns.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftPatternRepository extends JpaRepository<ShiftPattern, Long> {

       Optional<ShiftPatternProjection> findProjectionByExternalId (UUID externalId);

       
       Optional<ShiftPattern> findByExternalId (UUID externalId);

}
