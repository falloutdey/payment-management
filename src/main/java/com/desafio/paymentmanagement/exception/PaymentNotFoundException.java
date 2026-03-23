package com.desafio.paymentmanagement.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long id) {
        super("Pagamento não encontrado com ID: " + id);
    }
}