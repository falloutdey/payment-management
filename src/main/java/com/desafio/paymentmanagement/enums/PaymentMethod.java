package com.desafio.paymentmanagement.enums;

public enum PaymentMethod {
    BOLETO,
    PIX,
    CARTAO_CREDITO,
    CARTAO_DEBITO;

    public boolean requiresCardNumber() {
        return this == CARTAO_CREDITO || this == CARTAO_DEBITO;
    }
}