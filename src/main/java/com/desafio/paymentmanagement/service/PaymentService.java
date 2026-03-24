package com.desafio.paymentmanagement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desafio.paymentmanagement.dto.CreatePaymentRequest;
import com.desafio.paymentmanagement.dto.PaymentResponse;
import com.desafio.paymentmanagement.dto.UpdatePaymentStatusRequest;
import com.desafio.paymentmanagement.enums.PaymentStatus;
import com.desafio.paymentmanagement.exception.InvalidPaymentException;
import com.desafio.paymentmanagement.exception.InvalidPaymentStatusException;
import com.desafio.paymentmanagement.exception.PaymentNotFoundException;
import com.desafio.paymentmanagement.mapper.PaymentMapper;
import com.desafio.paymentmanagement.model.Payment;
import com.desafio.paymentmanagement.repository.PaymentRepository;
import com.desafio.paymentmanagement.repository.PaymentSpecification;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        validateCardNumber(request);
        Payment payment = paymentMapper.toEntity(request);
        Payment saved = paymentRepository.save(payment);
        return paymentMapper.toResponse(saved);
    }

    @Transactional
    public PaymentResponse updateStatus(Long id, UpdatePaymentStatusRequest request) {
        Payment payment = findPaymentById(id);
        validateStatusTransition(payment.getStatus(), request.getStatus());
        payment.setStatus(request.getStatus());
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listPayments(Integer debtCode, String payerDocument, PaymentStatus status) {
        return paymentRepository
                .findAll(PaymentSpecification.withFilters(debtCode, payerDocument, status))
                .stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePayment(Long id) {
        Payment payment = findPaymentById(id);

        if (payment.getStatus() != PaymentStatus.PENDENTE_DE_PROCESSAMENTO) {
            throw new InvalidPaymentStatusException(
                    "Somente pagamentos com status PENDENTE_DE_PROCESSAMENTO podem ser excluidos.");
        }

        payment.setStatus(PaymentStatus.INATIVO);
        paymentRepository.save(payment);
    }

    private Payment findPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    private void validateCardNumber(CreatePaymentRequest request) {
        boolean requiresCard = request.getPaymentMethod().requiresCardNumber();
        boolean hasCard = request.getCardNumber() != null && !request.getCardNumber().isBlank();

        if (requiresCard && !hasCard) {
            throw new InvalidPaymentException(
                    "O numero do cartao e obrigatorio para o metodo de pagamento: " + request.getPaymentMethod());
        }

        if (!requiresCard && hasCard) {
            throw new InvalidPaymentException(
                    "O numero do cartao nao deve ser informado para o metodo de pagamento: " + request.getPaymentMethod());
        }
    }

    private void validateStatusTransition(PaymentStatus current, PaymentStatus next) {
        switch (current) {
            case PENDENTE_DE_PROCESSAMENTO:
                if (next != PaymentStatus.PROCESSADO_COM_SUCESSO && next != PaymentStatus.PROCESSADO_COM_FALHA) {
                    throw new InvalidPaymentStatusException(
                            "Pagamento PENDENTE_DE_PROCESSAMENTO so pode ser alterado para PROCESSADO_COM_SUCESSO ou PROCESSADO_COM_FALHA.");
                }
                break;

            case PROCESSADO_COM_SUCESSO:
                throw new InvalidPaymentStatusException(
                        "Pagamento PROCESSADO_COM_SUCESSO nao pode ter seu status alterado.");

            case PROCESSADO_COM_FALHA:
                if (next != PaymentStatus.PENDENTE_DE_PROCESSAMENTO) {
                    throw new InvalidPaymentStatusException(
                            "Pagamento PROCESSADO_COM_FALHA so pode ser alterado para PENDENTE_DE_PROCESSAMENTO.");
                }
                break;

            case INATIVO:
                throw new InvalidPaymentStatusException(
                        "Pagamento INATIVO nao pode ter seu status alterado.");

            default:
                throw new InvalidPaymentStatusException("Transicao de status invalida.");
        }
    }
}