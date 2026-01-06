package org.example.reminderapp.repository.specification;

import org.example.reminderapp.dto.request.UserFilterDto;
import org.example.reminderapp.entity.User;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

// Class for hard filtres

public class UserSpecification {

    public static Specification<User> withFilters(UserFilterDto filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filters by username
            if (filters.getUsername() != null && !filters.getUsername().isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")),
                        "%" + filters.getUsername().toLowerCase() + "%"));
            }

            // Filters by email
            if (filters.getEmail() != null && !filters.getEmail().isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
                        "%" + filters.getEmail().toLowerCase() + "%"));
            }

            // Filters by firstname
            if (filters.getFirstname() != null && !filters.getFirstname().isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstname")),
                        "%" + filters.getFirstname().toLowerCase() + "%"));
            }

            // Filters by lastname
            if (filters.getLastname() != null && !filters.getLastname().isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastname")),
                        "%" + filters.getLastname().toLowerCase() + "%"));
            }

            // Filters by birthdate
            if (filters.getBirthDate() != null) {
                predicates.add(criteriaBuilder.equal(root.get("birthDate"), filters.getBirthDate()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
