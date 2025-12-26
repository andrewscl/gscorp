package com.gscorp.dv1.professions.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessionRepository extends JpaRepository<Profession, Long> {
    
    List<Profession> findAllById(Iterable<Long> ids);

}
