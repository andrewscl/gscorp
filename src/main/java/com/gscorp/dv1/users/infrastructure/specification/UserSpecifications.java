package com.gscorp.dv1.users.infrastructure.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.gscorp.dv1.enums.UserStatus;
import com.gscorp.dv1.users.infrastructure.User;

import jakarta.persistence.criteria.Predicate;

public class UserSpecifications {

    public static Specification<User> searchUsers(
            String q,
            UserStatus status) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            //Filtro global de texto
            if(q != null) {
                String likePattern = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("username")), likePattern),
                    cb.like(cb.lower(root.get("mail")), likePattern)
                ));
            }

            //Filtro de estado del enum
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
