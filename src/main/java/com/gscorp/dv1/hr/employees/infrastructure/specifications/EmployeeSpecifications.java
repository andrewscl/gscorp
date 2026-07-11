package com.gscorp.dv1.hr.employees.infrastructure.specifications;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.gscorp.dv1.enums.EmployeeStatus;
import com.gscorp.dv1.hr.employees.infrastructure.Employee;

import jakarta.persistence.criteria.Predicate;

public class EmployeeSpecifications {

    public static Specification<Employee> searchEmployees(
            String q,
            EmployeeStatus status) {
    
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            //Filtro global de texto
            if(q != null) {
                String likePattern = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), likePattern),
                    cb.like(cb.lower(root.get("fatherSurname")), likePattern),
                    cb.like(cb.lower(root.get("fatherSurname")), likePattern)
                ));
            }
            // Filtro de estado del enum
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
