package com.gscorp.dv1.attendance.infrastructure;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendancePunchRepo extends JpaRepository <AttendancePunch, Long>{

    Optional<AttendancePunch> findFirstByUserIdOrderByTsDesc(Long userId);
    List<AttendancePunch> findByUserIdAndTsBetweenOrderByTsAsc(Long userId, OffsetDateTime from, OffsetDateTime to);
    
}
