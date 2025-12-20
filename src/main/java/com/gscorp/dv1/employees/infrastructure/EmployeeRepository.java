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
          e.id AS id,
          e.photo_url AS photoUrl,
          e.name AS name,
          e.father_surname AS fatherSurname,
          e.mother_surname AS motherSurname,
          e.rut AS rut,
          e.mail AS mail,
          e.phone AS phone,
          pos.name AS positionName,
          e.active AS active,
          e.hire_date AS hireDate,
          e.created_at AS createdAt
        FROM employee e
        LEFT JOIN position pos ON pos.id = e.position_id
        JOIN employee_project ep ON e.id = ep.employee_id
        JOIN project p ON p.id = ep.project_id
        JOIN clients c ON c.id = p.client_id
        WHERE c.id = ANY(:clientIds)
          AND (
            :q IS NULL OR
            e.name ILIKE :q OR
            e.father_surname ILIKE :q OR
            e.mother_surname ILIKE :q OR
            e.rut ILIKE :q OR
            e.mail ILIKE :q OR
            e.phone ILIKE :q
          )
          AND (:active IS NULL OR e.active = :active)
        ORDER BY e.name ASC
        """,
      countQuery = """
        SELECT COUNT(DISTINCT e.id)
        FROM employee e
        JOIN employee_project ep ON e.id = ep.employee_id
        JOIN project p ON p.id = ep.project_id
        JOIN clients c ON c.id = p.client_id
        WHERE c.id = ANY(:clientIds)
          AND (
            :q IS NULL OR
            e.name ILIKE :q OR
            e.father_surname ILIKE :q OR
            e.mother_surname ILIKE :q OR
            e.rut ILIKE :q OR
            e.mail ILIKE :q OR
            e.phone ILIKE :q
          )
          AND (:active IS NULL OR e.active = :active)
        """,
      nativeQuery = true
    )
    Page<EmployeeTableProjection> findTableRowsForClientIds(
        @Param("clientIds") List<Long> clientIds,
        @Param("q") String q,           // expects pattern '%term%' or null
        @Param("active") Boolean active,
        Pageable pageable
    );

}
