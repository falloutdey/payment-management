package com.desafio.paymentmanagement.dto;

import com.desafio.paymentmanagement.enums.PaymentMethod;
import com.desafio.paymentmanagement.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {

    private Long id;
    private Integer debtCode;
    private String payerDocument;
    private PaymentMethod paymentMethod;
    private String cardNumber;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}