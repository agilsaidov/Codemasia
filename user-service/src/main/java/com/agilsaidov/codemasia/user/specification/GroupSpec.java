package com.agilsaidov.codemasia.user.specification;

import com.agilsaidov.codemasia.user.group.model.Group;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class GroupSpec {
    public static Specification<Group> withFilters(String name, Long creatorId, OffsetDateTime createdAt) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (!Long.class.equals(criteriaQuery.getResultType())) {
                root.fetch("createdBy", JoinType.INNER);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (creatorId != null) {
                predicates.add(criteriaBuilder.equal(root.get("createdBy").get("userId"), creatorId));
            }
            if (createdAt != null) {
                predicates.add(criteriaBuilder.between(root.get("createdAt"),
                        createdAt.toLocalDate().atStartOfDay().atOffset(createdAt.getOffset()),
                        createdAt.toLocalDate().plusDays(1).atStartOfDay().atOffset(createdAt.getOffset())));
            }

            criteriaQuery.distinct(true);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
