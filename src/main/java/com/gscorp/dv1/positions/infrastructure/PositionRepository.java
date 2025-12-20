package com.gscorp.dv1.positions.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    /**
     * Consulta explícita para que Spring Data no intente derivar el query a partir del nombre.
     * Devuelve la proyección PositionProjection mapeada a partir de la entidad Position.
     */
    @Query("SELECT p FROM Position p")
    List<PositionProjection> findAllProjection();


}
