package com.gscorp.dv1.admin.clients.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.admin.clients.infrastructure.projections.ClientProjection;
import com.gscorp.dv1.admin.clients.infrastructure.projections.ClientSelectProjection;
import com.gscorp.dv1.admin.clients.web.dto.ClientDto;
import com.gscorp.dv1.enums.ClientStatus;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>{

    List<Client> findByActiveTrueOrderByNameAsc();

    @EntityGraph(attributePaths = "users")
    Optional<Client> findById (Long id);
    
    @Query("""
         SELECT c.id
         FROM Client c
         JOIN c.users u
         WHERE u.externalId = :userExternalId          
          """)
    List<Long> findClientIdsByUserExternalId(
                              @Param("userExternalId") UUID userExternalId);

    /* -------- Opción C: devolver DTOs directamente (mejor rendimiento si sólo necesitas campos) --------
       Requiere constructor público en ClientDto con la firma usada abajo.
       Evita cargar entidades completas y relaciones perezosas.
    */
    @Query("select new com.gscorp.dv1.admin.clients.web.dto.ClientDto(c.id, c.externalId, c.name, c.legalName, c.taxId, c.contactEmail, c.status, c.active, c.company.name) "
         + "from Client c join c.users u where u.id = :userId order by c.name")
    List<ClientDto> findDtosByUserId(@Param("userId") Long userId);

    @Query("select c.id as id, c.name as name, c.active as active " +
       "from Client c join c.users u where u.id = :userId and c.active = true order by c.name")
    List<ClientSelectProjection> findClientsByUserId(Long userId);

      @Query("""
               SELECT
                  c.id AS id,
                  c.externalId AS externalId,
                  c.name AS name,
                  c.legalName AS legalName,
                  c.taxId AS taxId,
                  c.contactEmail AS contactEmail,
                  c.status AS status,
                  c.active AS active
               FROM Client c
               JOIN c.users u
               WHERE u.externalId = :userExternalId          
            """)
      List<ClientProjection> findClientsByUserExternalId(@Param("userExternalId") UUID userExternalId);


      @Query("SELECT c.id AS id, c.name AS name FROM Client c")
      List<ClientSelectProjection> findAllSelectProjections();

      Optional<Client> findByExternalId (UUID externalId);


      @Query("""
               SELECT
                  c.id AS id,
                  c.name AS name
               FROM Client c
               JOIN c.company cy
               WHERE cy.id IN :companyIds
                  AND (c.status = :status OR :status IS NULL)
            """)
      List<ClientSelectProjection> findClientsByStatusAndCompanies(
                     @Param("status") ClientStatus status,
                     @Param("companyIds") List<Long> companyIds
                     );

      @Query("""
               SELECT
                  c.id AS id,
                  c.externalId AS externalId,
                  c.name AS name,
                  c.legalName AS legalName,
                  c.taxId AS taxId,
                  c.contactEmail AS contactEmail,
                  c.status AS status,
                  c.active AS active
               FROM Client c
               JOIN c.company cy
               WHERE cy.externalId = :companyExternalId
                  AND (c.status = :status OR :status IS NULL)
            """)
      List<ClientProjection> findClientsByCompanyAndStatus(
                     @Param("companyExternalId") UUID companyExternalId,
                     @Param("status") ClientStatus status
                     );

      @Query("""
               SELECT
                  c.id AS id,
                  c.externalId AS externalId,
                  c.name AS name,
                  c.legalName AS legalName,
                  c.taxId AS taxId,
                  c.contactEmail AS contactEmail,
                  c.status AS status,
                  c.active AS active,
                  cy.name AS companyName
               FROM Client c
               JOIN c.company cy
               JOIN c.users u
               JOIN cy.users us
               WHERE cy.externalId = :companyExternalId
                  AND (c.status = :status OR :status IS NULL)
                  AND (u.externalId = :userExternalId)
                  AND (us.externalId = :userExternalId)
            """)
      List<ClientProjection> findClientsByCompanyAndStatusAndUserExternalId(
                     @Param("companyExternalId") UUID companyExternalId,
                     @Param("status") ClientStatus status,
                     @Param("userExternalId") UUID userExternalId
                     );

      @Query("""
               SELECT
                  c.id AS id,
                  c.externalId AS externalId,
                  c.name AS name,
                  c.legalName AS legalName,
                  c.taxId AS taxId,
                  c.contactEmail AS contactEmail,
                  c.status AS status,
                  c.active AS active
               FROM Client c
            """)
      List<ClientProjection> findAllProjections();

      @Query("""
               SELECT
                  c.id AS id,
                  c.externalId AS externalId,
                  c.name AS name,
                  c.legalName AS legalName,
                  c.taxId AS taxId,
                  c.contactEmail AS contactEmail,
                  c.status AS status,
                  c.active AS active
               FROM Client c
               WHERE c.externalId = :externalId
            """)
      Optional<ClientProjection> findProjectionByExternalId(
                     @Param("externalId") UUID externalId
                     );

}
