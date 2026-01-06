package org.example.reminderapp.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.example.reminderapp.dto.request.ReminderFilterDto;
import org.example.reminderapp.entity.Reminder;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

// Class for hard filtres

public class ReminderSpecification {

    public static Specification<Reminder> withFilters(ReminderFilterDto filter, Long currentUserId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), currentUserId));

            // Filters by title
            if (filter.getTitle() != null && !filter.getTitle().isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                        "%" + filter.getTitle().toLowerCase() + "%"));
            }

            // Filters by description
            if (filter.getDescription() != null && !filter.getDescription().isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                        "%" + filter.getDescription().toLowerCase() + "%"));
            }

            // Filters by start date
            if (filter.getRemindAtStart() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("remindAt"), filter.getRemindAtStart()));
            }

            // Filters by end date
            if (filter.getRemindAtEnd() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("remindAt"), filter.getRemindAtEnd()));
            }

            // Filters by status
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Filters by type
            if (filter.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), filter.getType()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
