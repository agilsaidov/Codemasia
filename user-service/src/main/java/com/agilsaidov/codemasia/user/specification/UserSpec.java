package com.agilsaidov.codemasia.user.specification;

import com.agilsaidov.codemasia.user.model.Role;
import com.agilsaidov.codemasia.user.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpec {
    public static Specification<User> withFilters(Role role) {
        return  (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
