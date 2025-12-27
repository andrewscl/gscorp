package com.gscorp.dv1.employees.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>{
    
    @EntityGraph(attributePaths = {"projects", "user"})
    Optional<Employee> findById(Long id);
    Optional<Employee> findByUserUsername(String username);

    @Query("SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.projects")
    List<Employee> findAllWithProjects();

    @Query("SELECT e FROM Employee e WHERE e.user IS NULL")
    List<Employee> findAllUnassignedEmployees();

    @EntityGraph(attributePaths = {"user", "projects", "position"})
    @Query("SELECT e FROM Employee e")
    List<Employee> findAllWithUserAndProjectsAndPosition();

    /**
     * Spring Data deriva la query y devuelve la proyección.
     * Nota importante: el nombre debe usar la propiedad anidada "user.id" => user_Id
     */
    Optional<EmployeeSelectProjection> findByUser_Id(Long userId);


    @Query("""
      SELECT
          e.id AS id,
          e.photoUrl AS photoUrl,
          e.name AS name,
          e.fatherSurname AS fatherSurname,
          e.motherSurname AS motherSurname,
          e.rut AS rut,
          e.mail AS mail,
          e.phone AS phone,
          p.name AS positionName,
          e.active AS active,
          e.hireDate AS hireDate,
          e.createdAt AS createdAt
      FROM Employee e
      LEFT JOIN e.position p
      WHERE (:q IS NULL OR LOWER(CONCAT(e.name,' ',e.fatherSurname,' ',e.motherSurname)) LIKE LOWER(CONCAT('%',:q,'%')))
          AND (:active IS NULL OR e.active = :active)
      ORDER BY e.name ASC
      """)
    Page<EmployeeTableProjection> findTableRows(
        @Param("q") String q,
        @Param("active") Boolean active,
        Pageable pageable
    );


    @Query(
      value = """
        SELECT DISTINCT
          e.id       AS id,
          e.photoUrl AS photoUrl,
          e.name     AS name,
          e.fatherSurname AS fatherSurname,
          e.motherSurname AS motherSurname,
          e.rut      AS rut,
          e.mail     AS mail,
          e.phone    AS phone,
          pos.name   AS positionName,
          e.active   AS active,
          e.hireDate AS hireDate,
          e.createdAt AS createdAt
        FROM Employee e
        LEFT JOIN e.position pos
        JOIN e.projects proj
        JOIN proj.client p
        WHERE p.id IN :clientIds
          AND (
            :q IS NULL OR
            LOWER(COALESCE(e.name, ''))       LIKE :q OR
            LOWER(COALESCE(e.fatherSurname, '')) LIKE :q OR
            LOWER(COALESCE(e.motherSurname, '')) LIKE :q OR
            LOWER(COALESCE(e.rut, ''))        LIKE :q OR
            LOWER(COALESCE(e.mail, ''))       LIKE :q OR
            LOWER(COALESCE(e.phone, ''))      LIKE :q
          )
          AND (:active IS NULL OR e.active = :active)
        ORDER BY e.name ASC
        """,
      countQuery = """
        SELECT COUNT(DISTINCT e.id)
        FROM Employee e
        JOIN e.projects proj
        JOIN proj.client p
        WHERE p.id IN :clientIds
          AND (
            :q IS NULL OR
            LOWER(COALESCE(e.name, ''))       LIKE :q OR
            LOWER(COALESCE(e.fatherSurname, '')) LIKE :q OR
            LOWER(COALESCE(e.motherSurname, '')) LIKE :q OR
            LOWER(COALESCE(e.rut, ''))        LIKE :q OR
            LOWER(COALESCE(e.mail, ''))       LIKE :q OR
            LOWER(COALESCE(e.phone, ''))      LIKE :q
          )
          AND (:active IS NULL OR e.active = :active)
        """
    )
    Page<EmployeeTableProjection> findTableRowsForClientIds(
        @Param("clientIds") List<Long> clientIds,
        @Param("q") String q,               // espera patrón ya preparado (ej. "%term%"), o null
        @Param("active") Boolean active,
        Pageable pageable
    );


    @Query("""
        SELECT e.id AS id,
              e.name AS name,
              e.fatherSurname AS fatherSurname,
              e.motherSurname AS motherSurname,
              e.rut AS rut,
              e.mail AS mail,
              e.phone AS phone,
              e.secondaryPhone AS secondaryPhone,
              e.hireDate AS hireDate,
              e.birthDate AS birthDate,
              e.exitDate AS exitDate,
              e.address AS address,
              e.active AS active,
              e.gender AS gender,
              n.id AS nationalityId,
              e.maritalStatus AS maritalStatus,
              e.studyLevel AS studyLevel,
              p.id AS professionId,
              e.previtionalSystem AS previtionalSystem,
              e.pensionEntity AS pensionEntity,
              e.healthSystem AS healthSystem,
              e.healthEntity AS healthEntity,
              e.paymentMethod AS paymentMethod,
              b.id AS bankId,
              e.bankAccountType AS bankAccountType,
              e.bankAccountNumber AS bankAccountNumber,
              e.contractType AS contractType,
              e.workSchedule AS workSchedule,
              e.shiftSystem AS shiftSystem,
              s.id AS shiftPatternId,
              pos.id AS positionId,
              (SELECT ARRAY_AGG(ep.project_id)
              FROM employee_project ep
              WHERE ep.employee_id = e.id) AS projectIds
        FROM Employee e
        JOIN e.nationality n
        JOIN e.professions p
        JOIN e.bank b
        JOIN e.shiftPattern s
        JOIN e.position pos
        WHERE e.id = :id
    """)
    Optional<EmployeeEditProjection> findEmployeeProjectionById(Long id);


}
