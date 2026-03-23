package com.desafio.paymentmanagement.dto;

import com.desafio.paymentmanagement.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreatePaymentRequest {

    @NotNull(message = "O código do débito é obrigatório")
    private Integer debtCode;

    @NotBlank(message = "O CPF/CNPJ do pagador é obrigatório")
    private String payerDocument;

    @NotNull(message = "O método de pagamento é obrigatório")
    private PaymentMethod paymentMethod;

    private String cardNumber;

    @NotNull(message = "O valor do pagamento é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor do pagamento deve ser maior que zero")
    private BigDecimal amount;
}