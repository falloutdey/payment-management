package com.desafio.paymentmanagement.dto;

import com.desafio.paymentmanagement.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePaymentStatusRequest {

    @NotNull(message = "O novo status é obrigatório")
    private PaymentStatus status;
}