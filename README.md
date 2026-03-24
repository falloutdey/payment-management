# API de Gestão de Pagamentos

Esta é uma API RESTful desenvolvida para gerenciar o recebimento e a atualização de status de pagamentos de débitos (pessoas físicas e jurídicas).

## Tecnologias Utilizadas

O projeto foi construído utilizando as seguintes tecnologias:

* **Java 17**
* **Spring Boot 3.5**
* **Banco de Dados:** H2 Database
* **Gerenciador de Dependências:** Maven

## Decisões de Arquitetura e Design

* **Padrão de Camadas:** O projeto segue o padrão `Controller -> Service -> Repository`, garantindo separação de responsabilidades.
* **Padrão DTO (Data Transfer Object):** Utilizado para isolar as entidades de banco de dados das requisições e respostas da API.
* **Tratamento Global de Exceções:** Implementado com `@RestControllerAdvice` para padronizar os retornos de erro (`400 Bad Request`, `404 Not Found`, `422 Unprocessable Entity`) de forma clara para o cliente.
* **Consultas Dinâmicas (Filtros):** Utilização da interface `JpaSpecificationExecutor` (Spring Data JPA) para construir consultas dinâmicas de forma limpa no endpoint de listagem.
* **Exclusão Lógica:** O método `DELETE` não apaga o registro fisicamente, mas altera seu status para `INATIVO`, preservando o histórico.

## Estrutura do Código e Classes Principais

Para refletir as decisões de design, a aplicação foi dividida nos seguintes pacotes e classes vitais:

* **`controller.PaymentController`**: Expõe os 4 endpoints da API (`POST` para criar, `PATCH` para atualizar status, `GET` para buscar com filtros e `DELETE` para exclusão lógica).
* **`service.PaymentService`**: Onde reside toda a regra de negócio.
  * Valida a obrigatoriedade do número do cartão na criação.
  * Valida as regras de transição de status (ex: não permite alterar um pagamento `PROCESSADO_COM_SUCESSO`).
* **`repository.PaymentRepository` & `PaymentSpecification`**: Interface de persistência e classe utilitária (Criteria API) responsável por aplicar os filtros dinâmicos de busca (código, documento ou status).
* **`model.Payment`**: Entidade JPA. Utiliza `@PrePersist` e `@PreUpdate` para gerenciar automaticamente as datas de criação e atualização, além de fixar o status inicial como `PENDENTE_DE_PROCESSAMENTO`.
* **`exception.GlobalExceptionHandler`**: Intercepta falhas de validação de DTOs e regras de negócio violadas (ex: `InvalidPaymentStatusException`), formatando a resposta para o objeto `ErrorResponse`.

## Como Executar

1. Faça o clone do repositório:
   `git clone https://github.com/SEU_USUARIO/payment-management.git`
   `cd payment-management`

2. Para executar a API, utilize o Maven Wrapper embutido:
   * No Linux/Mac: `./mvnw spring-boot:run`
   * No Windows: `mvnw.cmd spring-boot:run`

3. Para executar os testes automatizados (unitários e de integração):
   * No Linux/Mac: `./mvnw test`
   * No Windows: `mvnw.cmd test`

4. Acessos da Aplicação:
   * A API estará disponível na porta 8083: `http://localhost:8083/api/payments`
