package com.desafio.paymentmanagement.repository;

import com.desafio.paymentmanagement.enums.PaymentStatus;
import com.desafio.paymentmanagement.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    List<Payment> findByDebtCode(Integer debtCode);

    List<Payment> findByPayerDocument(String payerDocument);

    List<Payment> findByStatus(PaymentStatus status);
}