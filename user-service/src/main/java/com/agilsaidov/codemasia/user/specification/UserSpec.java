package com.agilsaidov.codemasia.user.specification;

import com.agilsaidov.codemasia.user.user.model.Role;
import com.agilsaidov.codemasia.user.user.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpec {
    public static Specification<User> withFilters(Role role, Boolean enabled,
                                                  String email, String username,
                                                  String name, String surname) {
        return  (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }
            if (enabled != null) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), enabled));
            }
            if (email != null && !email.isBlank()){
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }
            if (username != null && !username.isBlank()){
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
            }
            if (name != null && !name.isBlank()){
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (surname != null && !surname.isBlank()){
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("surname")), "%" + surname.toLowerCase() + "%"));
            }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
