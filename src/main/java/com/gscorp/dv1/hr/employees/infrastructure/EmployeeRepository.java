package com.gscorp.dv1.hr.employees.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.enums.EmployeeStatus;
import com.gscorp.dv1.enums.UserStatus;
import com.gscorp.dv1.hr.employees.infrastructure.Projections.EmployeeEditProjection;
import com.gscorp.dv1.hr.employees.infrastructure.Projections.EmployeeSelectProjection;
import com.gscorp.dv1.hr.employees.infrastructure.Projections.EmployeeTableProjection;
import com.gscorp.dv1.hr.employees.infrastructure.Projections.EmployeeViewProjection;
import com.gscorp.dv1.hr.employees.infrastructure.Projections.statistics.ClientEmployeesStatusSummaryProjection;
import com.gscorp.dv1.hr.employees.infrastructure.Projections.statistics.CompanyEmployeesStatusSummaryProjection;
import com.gscorp.dv1.hr.employees.infrastructure.Projections.statistics.CompanyEmployeesUserStatusSummaryProjection;
import com.gscorp.dv1.hr.employees.infrastructure.Projections.statistics.EmployeesStatusSummaryProjection;


@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>{
    
    @EntityGraph(attributePaths = {"projects", "user"})
    Optional<Employee> findById(Long id);

    @EntityGraph(attributePaths = {"projects", "user"})
    Optional<Employee> findByExternalId(UUID externalId);

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


    @Query("SELECT e.id AS id, e.name AS name, e.fatherSurname AS fatherSurname, e.motherSurname AS motherSurname FROM Employee e")
    List<EmployeeSelectProjection> findAllProjections();


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
          e.id            AS id,
          e.externalId    AS externalId,
          e.photoUrl      AS photoUrl,
          e.name          AS name,
          e.fatherSurname AS fatherSurname,
          e.motherSurname AS motherSurname,
          e.rut           AS rut,
          e.mail          AS mail,
          e.phone         AS phone,
          pos.name        AS positionName,
          e.status        AS status,
          e.hireDate      AS hireDate,
          e.createdAt     AS createdAt,
          usr.username    AS username,
          usr.mail        AS userMail,
          usr.phone       AS userPhone,
          usr.status      AS userStatus
        FROM Employee e
        LEFT JOIN e.position pos
        LEFT JOIN e.user usr
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
          AND (:status IS NULL OR e.status = :status)
          AND (
            (:showNotInvited = true AND usr IS NULL) OR
            (:showNotInvited = false AND (:userStatus IS NULL OR usr.status = :userStatus))
          )
        ORDER BY e.name ASC
        """,
      countQuery = """
        SELECT COUNT(DISTINCT e.id)
        FROM Employee e
        LEFT JOIN e.user usr
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
          AND (:status IS NULL OR e.status = :status)
          AND (
            (:showNotInvited = true AND usr IS NULL) OR
            (:showNotInvited = false AND (:userStatus IS NULL OR usr.status = :userStatus))
          )
        """
    )
    Page<EmployeeTableProjection> findTableRowsForClientIds(
        @Param("clientIds") List<Long> clientIds,
        @Param("q") String q,
        @Param("status") EmployeeStatus status,
        @Param("userStatus") UserStatus userStatus,
        @Param("showNotInvited") boolean showNotInvited,
        Pageable pageable
    );


    @Query("""
        SELECT e.id AS id,
              e.externalId AS externalId,
              e.name AS name,
              e.fatherSurname AS fatherSurname,
              e.motherSurname AS motherSurname,
              e.photoUrl AS photoUrl,
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
              pos.name AS position,
              c.name AS company,
              e.status AS status
        FROM Employee e
        JOIN e.nationality n
        JOIN e.professions p
        JOIN e.bank b
        JOIN e.shiftPattern s
        JOIN e.position pos
        JOIN e.company c
        WHERE e.externalId = :externalId
    """)
    Optional<EmployeeEditProjection> findEmployeeEditProjectionByExternalId(UUID externalId);


    @Query("""
        SELECT e.id AS id,
              e.externalId as externalId,
              e.name AS name,
              e.fatherSurname AS fatherSurname,
              e.motherSurname AS motherSurname,
              e.photoUrl AS photoUrl,
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
              n.name AS nationality,
              e.maritalStatus AS maritalStatus,
              e.studyLevel AS studyLevel,
              p.name AS profession,
              e.previtionalSystem AS previtionalSystem,
              e.pensionEntity AS pensionEntity,
              e.healthSystem AS healthSystem,
              e.healthEntity AS healthEntity,
              e.paymentMethod AS paymentMethod,
              b.name AS bank,
              e.bankAccountType AS bankAccountType,
              e.bankAccountNumber AS bankAccountNumber,
              e.contractType AS contractType,
              e.workSchedule AS workSchedule,
              e.shiftSystem AS shiftSystem,
              s.name AS shiftPattern,
              pos.name AS position,
              c.name AS company,
              e.status AS status
        FROM Employee e
        JOIN e.nationality n
        JOIN e.professions p
        JOIN e.bank b
        JOIN e.shiftPattern s
        JOIN e.position pos
        JOIN e.company c
        WHERE e.externalId = :externalId
    """)
    Optional<EmployeeViewProjection> findEmployeeViewProjectionByExternalId(UUID externalId);


    @Query("""
        SELECT e.id AS id,
              e.externalId as externalId,
              e.name AS name,
              e.fatherSurname AS fatherSurname,
              e.motherSurname AS motherSurname,
              e.photoUrl AS photoUrl,
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
              n.name AS nationality,
              e.maritalStatus AS maritalStatus,
              e.studyLevel AS studyLevel,
              p.name AS profession,
              e.previtionalSystem AS previtionalSystem,
              e.pensionEntity AS pensionEntity,
              e.healthSystem AS healthSystem,
              e.healthEntity AS healthEntity,
              e.paymentMethod AS paymentMethod,
              b.name AS bank,
              e.bankAccountType AS bankAccountType,
              e.bankAccountNumber AS bankAccountNumber,
              e.contractType AS contractType,
              e.workSchedule AS workSchedule,
              e.shiftSystem AS shiftSystem,
              s.name AS shiftPattern,
              pos.name AS position
        FROM Employee e
        JOIN e.nationality n
        JOIN e.professions p
        JOIN e.bank b
        JOIN e.shiftPattern s
        JOIN e.position pos
        WHERE e.id = :id
    """)
    Optional<EmployeeViewProjection> findEmployeeViewProjectionById(Long id);


    @Query(value = """
        SELECT 
            e.id AS id, 
            e.name AS name, 
            e.fatherSurname AS fatherSurname, 
            e.motherSurname AS motherSurname,
            u.id AS userId
        FROM Employee e
        JOIN e.user u
        WHERE e.id = :id
        """)
    Optional<EmployeeSelectProjection> findEmployeeSelectDtoById(@Param("id") Long id);


    @Query(value = """
        SELECT 
            e.id AS id, 
            e.name AS name, 
            e.fatherSurname AS fatherSurname, 
            e.motherSurname AS motherSurname,
            u.id AS userId
        FROM Employee e
        JOIN e.user u
        WHERE u.externalId = :externalId
        """)
    Optional<EmployeeSelectProjection> findByUserExternalId(@Param("externalId") UUID externalId);


    @Query("""
        SELECT 
            c.name AS companyName,
            COALESCE(SUM(CASE WHEN e.status = 'HIRED' THEN 1 ELSE 0 END), 0L) AS hiredCount,
            COALESCE(SUM(CASE WHEN e.status = 'ACTIVE' THEN 1 ELSE 0 END), 0L) AS activeCount,
            COALESCE(SUM(CASE WHEN e.status = 'NOTICE_GIVEN' THEN 1 ELSE 0 END), 0L) AS noticeGivenCount,
            COALESCE(SUM(CASE WHEN e.status = 'INACTIVE' THEN 1 ELSE 0 END), 0L) AS inactiveCount,
            COALESCE(SUM(CASE WHEN e.status = 'SETTLED' THEN 1 ELSE 0 END), 0L) AS settledCount
        FROM Employee e
        JOIN e.projects p
        JOIN p.client cl
        LEFT JOIN e.company c
        WHERE p.id IN :clientIds
        GROUP BY c.name
    """)
    List<CompanyEmployeesStatusSummaryProjection> getCompanyEmployeesStat(
        @Param("clientIds") List<Long> clientIds        
    );


    @Query("""
        SELECT
            cl.name AS clientName,
            COALESCE(SUM(CASE WHEN e.status = 'HIRED' THEN 1 ELSE 0 END), 0L) AS hiredCount,
            COALESCE(SUM(CASE WHEN e.status = 'ACTIVE' THEN 1 ELSE 0 END), 0L) AS activeCount,
            COALESCE(SUM(CASE WHEN e.status = 'NOTICE_GIVEN' THEN 1 ELSE 0 END), 0L) AS noticeGivenCount,
            COALESCE(SUM(CASE WHEN e.status = 'INACTIVE' THEN 1 ELSE 0 END), 0L) AS inactiveCount,
            COALESCE(SUM(CASE WHEN e.status = 'SETTLED' THEN 1 ELSE 0 END), 0L) AS settledCount
        FROM Employee e
        JOIN e.projects p
        JOIN p.client cl
        WHERE p.id IN :clientIds
        GROUP BY cl.name
    """)
    List<ClientEmployeesStatusSummaryProjection> getClientEmployeesStat(
        @Param("clientIds") List<Long> clientIds
    );


    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN e.status = 'HIRED' THEN 1 ELSE 0 END), 0L) AS hiredCount,
            COALESCE(SUM(CASE WHEN e.status = 'ACTIVE' THEN 1 ELSE 0 END), 0L) AS activeCount,
            COALESCE(SUM(CASE WHEN e.status = 'NOTICE_GIVEN' THEN 1 ELSE 0 END), 0L) AS noticeGivenCount,
            COALESCE(SUM(CASE WHEN e.status = 'INACTIVE' THEN 1 ELSE 0 END), 0L) AS inactiveCount,
            COALESCE(SUM(CASE WHEN e.status = 'SETTLED' THEN 1 ELSE 0 END), 0L) AS settledCount
        FROM Employee e
        JOIN e.projects proj
        JOIN proj.client p
        WHERE p.id IN :clientIds
    """)
    List<EmployeesStatusSummaryProjection> getEmployeesStatusSummary(
        @Param("clientIds") List<Long> clientIds
    );


    @Query("""
        SELECT
            c.name AS companyName,
            COALESCE(SUM(CASE WHEN usr IS NULL THEN 1 ELSE 0 END), 0L) AS notInvitedCount,
            COALESCE(SUM(CASE WHEN usr IS NOT NULL AND usr.status = 'INVITED' THEN 1 ELSE 0 END), 0L) AS invitedCount,
            COALESCE(SUM(CASE WHEN usr IS NOT NULL AND usr.status = 'ACTIVE' THEN 1 ELSE 0 END), 0L) AS activeCount,
            COALESCE(SUM(CASE WHEN usr IS NOT NULL AND usr.status = 'INACTIVE' THEN 1 ELSE 0 END), 0L) AS inactiveCount,
            COALESCE(SUM(CASE WHEN usr IS NOT NULL AND usr.status = 'EXPIRED' THEN 1 ELSE 0 END), 0L) AS expiredCount,
            COALESCE(SUM(CASE WHEN usr IS NOT NULL AND usr.status = 'SUSPENDED' THEN 1 ELSE 0 END), 0L) AS suspendedCount
        FROM Employee e
        LEFT JOIN e.user usr
        LEFT JOIN e.company c
        JOIN e.projects proj
        JOIN proj.client p
        WHERE p.id IN :clientIds
        GROUP BY c.name
    """)
    List<CompanyEmployeesUserStatusSummaryProjection> findEmployeesUserStatusSummary(
        @Param("clientIds") List<Long> clientIds
    );


    @Query(value = """
        SELECT 
            e.id AS id,
            e.externalId AS externalId, 
            e.name AS name, 
            e.fatherSurname AS fatherSurname, 
            e.motherSurname AS motherSurname,
            u.id AS userId
        FROM Employee e
        LEFT JOIN e.user u
        WHERE e.status = :status
        ORDER BY e.fatherSurname ASC
        """)
    List<EmployeeSelectProjection>
            findByStatus(@Param("status") EmployeeStatus status);


    @Query(
        value = """
        SELECT DISTINCT e
        FROM Employee e
        LEFT JOIN FETCH e.company 
        LEFT JOIN FETCH e.projects p
        LEFT JOIN FETCH p.client c
        WHERE e.externalId = :externalId
        """)
    Optional<Employee> findForInvitationByExternalId(@Param("externalId") UUID externalId);

}
