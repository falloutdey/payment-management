package com.desafio.paymentmanagement.controller;

import com.desafio.paymentmanagement.dto.CreatePaymentRequest;
import com.desafio.paymentmanagement.dto.PaymentResponse;
import com.desafio.paymentmanagement.dto.UpdatePaymentStatusRequest;
import com.desafio.paymentmanagement.enums.PaymentStatus;
import com.desafio.paymentmanagement.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * POST /api/payments
     * Cria um novo pagamento com status PENDENTE_DE_PROCESSAMENTO
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PATCH /api/payments/{id}/status
     * Atualiza o status de um pagamento
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {
        return ResponseEntity.ok(paymentService.updateStatus(id, request));
    }

    /**
     * GET /api/payments
     * Lista pagamentos com filtros opcionais: debtCode, payerDocument, status
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> listPayments(
            @RequestParam(required = false) Integer debtCode,
            @RequestParam(required = false) String payerDocument,
            @RequestParam(required = false) PaymentStatus status) {
        return ResponseEntity.ok(paymentService.listPayments(debtCode, payerDocument, status));
    }

    /**
     * DELETE /api/payments/{id}
     * Exclusão lógica: muda status para INATIVO (somente se PENDENTE_DE_PROCESSAMENTO)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}