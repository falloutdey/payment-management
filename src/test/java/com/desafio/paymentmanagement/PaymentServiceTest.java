package com.desafio.paymentmanagement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.desafio.paymentmanagement.dto.CreatePaymentRequest;
import com.desafio.paymentmanagement.dto.PaymentResponse;
import com.desafio.paymentmanagement.dto.UpdatePaymentStatusRequest;
import com.desafio.paymentmanagement.enums.PaymentMethod;
import com.desafio.paymentmanagement.enums.PaymentStatus;
import com.desafio.paymentmanagement.exception.InvalidPaymentException;
import com.desafio.paymentmanagement.exception.InvalidPaymentStatusException;
import com.desafio.paymentmanagement.exception.PaymentNotFoundException;
import com.desafio.paymentmanagement.mapper.PaymentMapper;
import com.desafio.paymentmanagement.model.Payment;
import com.desafio.paymentmanagement.repository.PaymentRepository;
import com.desafio.paymentmanagement.service.PaymentService;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentService paymentService;

    private Payment pendingPayment;

    @BeforeEach
    void setUp() {
        pendingPayment = Payment.builder()
                .id(1L)
                .debtCode(123)
                .payerDocument("12345678900")
                .paymentMethod(PaymentMethod.PIX)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.PENDENTE_DE_PROCESSAMENTO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // CREATE

    @Test
    @DisplayName("Deve criar pagamento PIX sem número de cartão")
    void createPayment_pix_success() {
        CreatePaymentRequest request = pixRequest();
        Payment entity = pendingPayment;
        PaymentResponse response = mockResponse(entity);

        when(paymentMapper.toEntity(request)).thenReturn(entity);
        when(paymentRepository.save(entity)).thenReturn(entity);
        when(paymentMapper.toResponse(entity)).thenReturn(response);

        PaymentResponse result = paymentService.createPayment(request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDENTE_DE_PROCESSAMENTO);
        verify(paymentRepository).save(any());
    }

    @Test
    @DisplayName("Deve criar pagamento com cartão de crédito quando número informado")
    void createPayment_creditCard_success() {
        CreatePaymentRequest request = creditCardRequest("4111111111111111");
        Payment entity = Payment.builder()
                .id(2L)
                .debtCode(456)
                .payerDocument("12345678900")
                .paymentMethod(PaymentMethod.CARTAO_CREDITO)
                .cardNumber("4111111111111111")
                .amount(new BigDecimal("200.00"))
                .status(PaymentStatus.PENDENTE_DE_PROCESSAMENTO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentMapper.toEntity(request)).thenReturn(entity);
        when(paymentRepository.save(entity)).thenReturn(entity);
        when(paymentMapper.toResponse(entity)).thenReturn(mockResponse(entity));

        assertThatNoException().isThrownBy(() -> paymentService.createPayment(request));
    }

    @Test
    @DisplayName("Deve lançar exceção quando cartão de crédito sem número")
    void createPayment_creditCard_missingCardNumber() {
        CreatePaymentRequest request = creditCardRequest(null);

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessageContaining("número do cartão é obrigatório");
    }

    @Test
    @DisplayName("Deve lançar exceção quando PIX com número de cartão informado")
    void createPayment_pix_withCardNumber() {
        CreatePaymentRequest request = pixRequest();
        request.setCardNumber("4111111111111111");

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessageContaining("não deve ser informado");
    }

    // UPDATE STATUS

    @Test
    @DisplayName("Deve atualizar de PENDENTE para PROCESSADO_COM_SUCESSO")
    void updateStatus_pendingToSuccess() {
        UpdatePaymentStatusRequest request = statusRequest(PaymentStatus.PROCESSADO_COM_SUCESSO);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any())).thenReturn(pendingPayment);
        when(paymentMapper.toResponse(any())).thenReturn(mockResponse(pendingPayment));

        assertThatNoException().isThrownBy(() -> paymentService.updateStatus(1L, request));
    }

    @Test
    @DisplayName("Deve atualizar de PENDENTE para PROCESSADO_COM_FALHA")
    void updateStatus_pendingToFailure() {
        UpdatePaymentStatusRequest request = statusRequest(PaymentStatus.PROCESSADO_COM_FALHA);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any())).thenReturn(pendingPayment);
        when(paymentMapper.toResponse(any())).thenReturn(mockResponse(pendingPayment));

        assertThatNoException().isThrownBy(() -> paymentService.updateStatus(1L, request));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar alterar PROCESSADO_COM_SUCESSO")
    void updateStatus_successCannotChange() {
        pendingPayment.setStatus(PaymentStatus.PROCESSADO_COM_SUCESSO);
        UpdatePaymentStatusRequest request = statusRequest(PaymentStatus.PENDENTE_DE_PROCESSAMENTO);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));

        assertThatThrownBy(() -> paymentService.updateStatus(1L, request))
                .isInstanceOf(InvalidPaymentStatusException.class)
                .hasMessageContaining("PROCESSADO_COM_SUCESSO não pode ter seu status alterado");
    }

    @Test
    @DisplayName("Deve atualizar de PROCESSADO_COM_FALHA para PENDENTE_DE_PROCESSAMENTO")
    void updateStatus_failureToPending() {
        pendingPayment.setStatus(PaymentStatus.PROCESSADO_COM_FALHA);
        UpdatePaymentStatusRequest request = statusRequest(PaymentStatus.PENDENTE_DE_PROCESSAMENTO);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any())).thenReturn(pendingPayment);
        when(paymentMapper.toResponse(any())).thenReturn(mockResponse(pendingPayment));

        assertThatNoException().isThrownBy(() -> paymentService.updateStatus(1L, request));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar alterar de PROCESSADO_COM_FALHA para SUCESSO")
    void updateStatus_failureToSuccessIsInvalid() {
        pendingPayment.setStatus(PaymentStatus.PROCESSADO_COM_FALHA);
        UpdatePaymentStatusRequest request = statusRequest(PaymentStatus.PROCESSADO_COM_SUCESSO);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));

        assertThatThrownBy(() -> paymentService.updateStatus(1L, request))
                .isInstanceOf(InvalidPaymentStatusException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção quando pagamento não encontrado ao atualizar status")
    void updateStatus_paymentNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.updateStatus(99L, statusRequest(PaymentStatus.PROCESSADO_COM_SUCESSO)))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    // DELETE (exclusão lógica)

    @Test
    @DisplayName("Deve realizar exclusão lógica de pagamento PENDENTE")
    void deletePayment_success() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any())).thenReturn(pendingPayment);

        assertThatNoException().isThrownBy(() -> paymentService.deletePayment(1L));
        assertThat(pendingPayment.getStatus()).isEqualTo(PaymentStatus.INATIVO);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir pagamento PROCESSADO_COM_SUCESSO")
    void deletePayment_notPending() {
        pendingPayment.setStatus(PaymentStatus.PROCESSADO_COM_SUCESSO);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));

        assertThatThrownBy(() -> paymentService.deletePayment(1L))
                .isInstanceOf(InvalidPaymentStatusException.class)
                .hasMessageContaining("PENDENTE_DE_PROCESSAMENTO");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir pagamento inexistente")
    void deletePayment_notFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.deletePayment(99L))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    // Helpers

    private CreatePaymentRequest pixRequest() {
        CreatePaymentRequest r = new CreatePaymentRequest();
        r.setDebtCode(123);
        r.setPayerDocument("12345678900");
        r.setPaymentMethod(PaymentMethod.PIX);
        r.setAmount(new BigDecimal("100.00"));
        return r;
    }

    private CreatePaymentRequest creditCardRequest(String cardNumber) {
        CreatePaymentRequest r = new CreatePaymentRequest();
        r.setDebtCode(456);
        r.setPayerDocument("12345678900");
        r.setPaymentMethod(PaymentMethod.CARTAO_CREDITO);
        r.setCardNumber(cardNumber);
        r.setAmount(new BigDecimal("200.00"));
        return r;
    }

    private UpdatePaymentStatusRequest statusRequest(PaymentStatus status) {
        UpdatePaymentStatusRequest r = new UpdatePaymentStatusRequest();
        r.setStatus(status);
        return r;
    }

    private PaymentResponse mockResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .debtCode(payment.getDebtCode())
                .payerDocument(payment.getPayerDocument())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}