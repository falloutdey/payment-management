package com.desafio.paymentmanagement.repository;

import com.desafio.paymentmanagement.enums.PaymentStatus;
import com.desafio.paymentmanagement.model.Payment;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PaymentSpecification {

    private PaymentSpecification() {}

    public static Specification<Payment> withFilters(Integer debtCode, String payerDocument, PaymentStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (debtCode != null) {
                predicates.add(criteriaBuilder.equal(root.get("debtCode"), debtCode));
            }

            if (payerDocument != null && !payerDocument.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("payerDocument"), payerDocument));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}