package com.gscorp.dv1.companies.infrastructure.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.gscorp.dv1.companies.infrastructure.Company;
import com.gscorp.dv1.enums.CompanyStatus;

import jakarta.persistence.criteria.Predicate;

public class CompanySpecifications {

    public static Specification<Company> searchCompanies(
            String q,
            CompanyStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            //Filtro global de texto
            if(q != null) {
                String likePattern = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), likePattern),
                    cb.like(cb.lower(root.get("legalname")), likePattern),
                    cb.like(cb.lower(root.get("taxId")), likePattern)
                ));
            }
            //Filtro de estado del enum
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Company> belongsToUser(Long userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return cb.conjunction(); // No filter if userId is null
            }
            return cb.equal(root.join("users").get("id"), userId);
        };
    }
    
}
