package com.gscorp.dv1.clients.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.clients.web.dto.ClientDto;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>{

    List<Client> findByActiveTrueOrderByNameAsc();

    @EntityGraph(attributePaths = "users")
    Optional<Client> findById (Long id);
    
    @Query("select c.id from Client c join c.users u where u.id = :userId")
    List<Long> findClientIdsByUserId(Long userId);

    /* -------- Opción C: devolver DTOs directamente (mejor rendimiento si sólo necesitas campos) --------
       Requiere constructor público en ClientDto con la firma usada abajo.
       Evita cargar entidades completas y relaciones perezosas.
    */
    @Query("select new com.gscorp.dv1.clients.web.dto.ClientDto(c.id, c.name, c.legalName, c.taxId, c.contactEmail, c.active) "
         + "from Client c join c.users u where u.id = :userId order by c.name")
    List<ClientDto> findDtosByUserId(@Param("userId") Long userId);

    @Query("select c.id as id, c.name as name, c.active as active " +
       "from Client c join c.users u where u.id = :userId and c.active = true order by c.name")
    List<ClientSelectProjection> findClientsByUserId(Long userId);
    
    
}
