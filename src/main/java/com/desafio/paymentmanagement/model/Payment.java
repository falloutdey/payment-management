package com.desafio.paymentmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.desafio.paymentmanagement.enums.PaymentMethod;
import com.desafio.paymentmanagement.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "debt_code", nullable = false)
    private Integer debtCode;

    @Column(name = "payer_document", nullable = false, length = 18)
    private String payerDocument;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Payment() {}

    public Payment(Long id, Integer debtCode, String payerDocument, PaymentMethod paymentMethod, String cardNumber, BigDecimal amount, PaymentStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.debtCode = debtCode;
        this.payerDocument = payerDocument;
        this.paymentMethod = paymentMethod;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = PaymentStatus.PENDENTE_DE_PROCESSAMENTO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Integer getDebtCode() {
        return debtCode;
    }

    public String getPayerDocument() {
        return payerDocument;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDebtCode(Integer debtCode) {
        this.debtCode = debtCode;
    }

    public void setPayerDocument(String payerDocument) {
        this.payerDocument = payerDocument;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long id;
        private Integer debtCode;
        private String payerDocument;
        private PaymentMethod paymentMethod;
        private String cardNumber;
        private BigDecimal amount;
        private PaymentStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder debtCode(Integer debtCode) {
            this.debtCode = debtCode;
            return this;
        }

        public Builder payerDocument(String payerDocument) {
            this.payerDocument = payerDocument;
            return this;
        }

        public Builder paymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder cardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder status(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Payment build() {
            return new Payment(id, debtCode, payerDocument, paymentMethod, cardNumber, amount, status, createdAt, updatedAt);
        }
    }
}