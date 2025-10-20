package com.gscorp.dv1.requests.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRequestRepository
            extends JpaRepository<ShiftRequest, Long> {

    @EntityGraph(attributePaths = "site")
    Optional<ShiftRequest> findById(Long id);

}
