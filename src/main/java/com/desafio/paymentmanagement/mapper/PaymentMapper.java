package com.desafio.paymentmanagement.mapper;

import com.desafio.paymentmanagement.dto.CreatePaymentRequest;
import com.desafio.paymentmanagement.dto.PaymentResponse;
import com.desafio.paymentmanagement.enums.PaymentStatus;
import com.desafio.paymentmanagement.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toEntity(CreatePaymentRequest request) {
        return Payment.builder()
                .debtCode(request.getDebtCode())
                .payerDocument(request.getPayerDocument())
                .paymentMethod(request.getPaymentMethod())
                .cardNumber(request.getCardNumber())
                .amount(request.getAmount())
                .status(PaymentStatus.PENDENTE_DE_PROCESSAMENTO)
                .build();
    }

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .debtCode(payment.getDebtCode())
                .payerDocument(payment.getPayerDocument())
                .paymentMethod(payment.getPaymentMethod())
                .cardNumber(payment.getCardNumber())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}