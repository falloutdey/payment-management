package com.desafio.paymentmanagement;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.desafio.paymentmanagement.enums.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/payments";

    // -------------------------------------------------------------------------
    // POST - criar pagamento
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/payments - cria pagamento PIX com sucesso")
    void createPayment_pix_returns201() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pixPayload()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("PENDENTE_DE_PROCESSAMENTO"))
                .andExpect(jsonPath("$.paymentMethod").value("PIX"));
    }

    @Test
    @DisplayName("POST /api/payments - cria pagamento cartão de crédito com sucesso")
    void createPayment_creditCard_returns201() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(creditCardPayload("4111111111111111")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE_DE_PROCESSAMENTO"));
    }

    @Test
    @DisplayName("POST /api/payments - falha sem campo obrigatório (valor)")
    void createPayment_missingAmount_returns400() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "debtCode", 123,
                "payerDocument", "12345678900",
                "paymentMethod", "PIX"
        ));
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/payments - falha cartão sem número")
    void createPayment_creditCardWithoutNumber_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(creditCardPayload(null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/payments - falha PIX com número de cartão")
    void createPayment_pixWithCardNumber_returns400() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "debtCode", 123,
                "payerDocument", "12345678900",
                "paymentMethod", "PIX",
                "cardNumber", "4111111111111111",
                "amount", 100.00
        ));
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // PATCH - atualizar status
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /api/payments/{id}/status - PENDENTE -> SUCESSO")
    void updateStatus_pendingToSuccess() throws Exception {
        Long id = createPixPayment();

        mockMvc.perform(patch(BASE_URL + "/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusPayload(PaymentStatus.PROCESSADO_COM_SUCESSO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSADO_COM_SUCESSO"));
    }

    @Test
    @DisplayName("PATCH /api/payments/{id}/status - SUCESSO não pode ser alterado")
    void updateStatus_successIsImmutable() throws Exception {
        Long id = createPixPayment();
        updateStatusTo(id, PaymentStatus.PROCESSADO_COM_SUCESSO);

        mockMvc.perform(patch(BASE_URL + "/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusPayload(PaymentStatus.PENDENTE_DE_PROCESSAMENTO)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("PATCH /api/payments/{id}/status - FALHA -> PENDENTE")
    void updateStatus_failureToPending() throws Exception {
        Long id = createPixPayment();
        updateStatusTo(id, PaymentStatus.PROCESSADO_COM_FALHA);

        mockMvc.perform(patch(BASE_URL + "/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusPayload(PaymentStatus.PENDENTE_DE_PROCESSAMENTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE_DE_PROCESSAMENTO"));
    }

    @Test
    @DisplayName("PATCH /api/payments/{id}/status - pagamento inexistente retorna 404")
    void updateStatus_notFound() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusPayload(PaymentStatus.PROCESSADO_COM_SUCESSO)))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // GET - listar e filtrar
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/payments - lista todos os pagamentos")
    void listPayments_returnsAll() throws Exception {
        createPixPayment();
        createPixPayment();

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("GET /api/payments?debtCode=123 - filtra por código de débito")
    void listPayments_filterByDebtCode() throws Exception {
        createPixPayment();

        mockMvc.perform(get(BASE_URL).param("debtCode", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].debtCode", everyItem(equalTo(123))));
    }

    @Test
    @DisplayName("GET /api/payments?status=PENDENTE_DE_PROCESSAMENTO - filtra por status")
    void listPayments_filterByStatus() throws Exception {
        createPixPayment();

        mockMvc.perform(get(BASE_URL).param("status", "PENDENTE_DE_PROCESSAMENTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status", everyItem(equalTo("PENDENTE_DE_PROCESSAMENTO"))));
    }

    @Test
    @DisplayName("GET /api/payments?payerDocument=12345678900 - filtra por CPF")
    void listPayments_filterByPayerDocument() throws Exception {
        createPixPayment();

        mockMvc.perform(get(BASE_URL).param("payerDocument", "12345678900"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].payerDocument", everyItem(equalTo("12345678900"))));
    }

    // -------------------------------------------------------------------------
    // DELETE - exclusão lógica
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/payments/{id} - exclusão lógica de pagamento PENDENTE")
    void deletePayment_success() throws Exception {
        Long id = createPixPayment();

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL).param("status", "INATIVO"))
                .andExpect(jsonPath("$[?(@.id == " + id + ")].status", hasItem("INATIVO")));
    }

    @Test
    @DisplayName("DELETE /api/payments/{id} - falha ao excluir pagamento PROCESSADO_COM_SUCESSO")
    void deletePayment_notPending() throws Exception {
        Long id = createPixPayment();
        updateStatusTo(id, PaymentStatus.PROCESSADO_COM_SUCESSO);

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("DELETE /api/payments/{id} - pagamento inexistente retorna 404")
    void deletePayment_notFound() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Long createPixPayment() throws Exception {
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pixPayload()))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private void updateStatusTo(Long id, PaymentStatus status) throws Exception {
        mockMvc.perform(patch(BASE_URL + "/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusPayload(status)));
    }

    private String pixPayload() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "debtCode", 123,
                "payerDocument", "12345678900",
                "paymentMethod", "PIX",
                "amount", 100.00
        ));
    }

    private String creditCardPayload(String cardNumber) throws Exception {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("debtCode", 456);
        map.put("payerDocument", "12345678900");
        map.put("paymentMethod", "CARTAO_CREDITO");
        map.put("amount", 200.00);
        if (cardNumber != null) map.put("cardNumber", cardNumber);
        return objectMapper.writeValueAsString(map);
    }

    private String statusPayload(PaymentStatus status) throws Exception {
        return objectMapper.writeValueAsString(Map.of("status", status.name()));
    }
}