package com.agilsaidov.codemasia.exam.specification;

import com.agilsaidov.codemasia.exam.model.Difficulty;
import com.agilsaidov.codemasia.exam.model.Problem;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ProblemSpec {
    public static Specification<Problem> withFilters(String examId, String title,
                                                     Difficulty difficulty, OffsetDateTime createdAt,
                                                     Integer point) {

        return ((root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (examId != null) {
                predicates.add(criteriaBuilder.equal(root.get("exam").get("examId"), examId));
            }

            if(title != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + title.toLowerCase() + "%"
                ));
            }

            if(difficulty != null) {
                predicates.add(criteriaBuilder.equal(root.get("difficulty"), difficulty));
            }

            if(point != null) {
                predicates.add(criteriaBuilder.equal(root.get("point"), point));
            }

            if (createdAt != null) {
                predicates.add(criteriaBuilder.between(root.get("createdAt"),
                        createdAt.toLocalDate().atStartOfDay().atOffset(createdAt.getOffset()),
                        createdAt.toLocalDate().plusDays(1).atStartOfDay().atOffset(createdAt.getOffset())));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }
}
