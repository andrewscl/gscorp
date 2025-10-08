package com.gscorp.dv1.patrol.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatrolRouteRepo extends JpaRepository<PatrolRoute, Long>{

    List<PatrolRoute> findBySiteId(Long siteId);

}
