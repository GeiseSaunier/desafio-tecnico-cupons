# Coupon API

API REST para gerenciamento de cupons de desconto, desenvolvida com Java 17, Spring Boot e banco de dados H2 em memória.

---

## Tecnologias

- Java 17
- Spring Boot 4.0.7
- Spring Data JPA
- Spring Validation
- H2 Database (in-memory)
- Springdoc OpenAPI (Swagger)
- JUnit 5
- Docker

---

## Como rodar a aplicação

### Opção 1 — Docker (recomendado)

> Não é necessário ter Java ou Maven instalado.

```bash
docker compose up --build
```

Aguarde a mensagem:
```
Started DesafioTecnicoApplication in X seconds
```

Para parar:
```bash
docker compose down
```

---

### Opção 2 — Localmente

> Necessário ter Java 17+ instalado.

**Linux/macOS:**
```bash
./mvnw spring-boot:run
```

**Windows:**
```bash
mvnw.cmd spring-boot:run
```

---

## Acessos

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |

**Configurações do H2 Console:**

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:desafio_tecnico` |
| User Name | `sa` |
| Password | *(deixar em branco)* |

> Os dados são perdidos ao reiniciar a aplicação por ser um banco em memória.

---

## Endpoints

### POST /coupon — Cadastrar cupom

**Request:**
```json
{
  "code": "ABC123",
  "description": "Cupom de 10% de desconto",
  "discountValue": 10.00,
  "expirationDate": "2027-12-31T00:00:00.000Z",
  "published": true
}
```

**Regras:**

| Campo | Regra |
|---|---|
| `code` | Exatamente 6 caracteres alfanuméricos. Caracteres especiais são removidos automaticamente e letras convertidas para maiúsculas |
| `description` | Obrigatório |
| `discountValue` | Mínimo de `0.5` |
| `expirationDate` | Não pode ser uma data no passado. Formato: `yyyy-MM-ddTHH:mm:ss.SSSZ` |
| `published` | `true` → status `ACTIVE` / `false` ou omitido → status `INACTIVE` |

**Response — 201 Created:**
```json
{
  "id": "c1963490-42b6-4a9b-8ad8-baf7847da0af",
  "code": "ABC123",
  "description": "Cupom de 10% de desconto",
  "discountValue": 10.00,
  "expirationDate": "2027-12-31T00:00:00.000Z",
  "published": true,
  "redeemed": false,
  "status": "ACTIVE"
}
```

---

### GET /coupon — Listar cupons

Retorna todos os cupons cadastrados, incluindo os deletados.

**Response — 200 OK:**
```json
[
  {
    "id": "c1963490-42b6-4a9b-8ad8-baf7847da0af",
    "code": "ABC123",
    "description": "Cupom de 10% de desconto",
    "discountValue": 10.00,
    "expirationDate": "2027-12-31T00:00:00.000Z",
    "published": true,
    "redeemed": false,
    "status": "ACTIVE"
  }
]
```

---

### DELETE /coupon/{id} — Deletar cupom

Realiza um **soft delete**: o cupom não é removido do banco, apenas tem seu status alterado para `DELETED`, garantindo a preservação dos dados.

**Response — 204 No Content**

---

## Status do cupom

| Status | Descrição |
|---|---|
| `ACTIVE` | Cupom publicado e ativo |
| `INACTIVE` | Cupom cadastrado mas não publicado |
| `DELETED` | Cupom deletado (soft delete) |

---

## Códigos de resposta

| Código | Situação |
|---|---|
| `201 Created` | Cupom cadastrado com sucesso |
| `200 OK` | Listagem retornada com sucesso |
| `204 No Content` | Cupom deletado com sucesso |
| `400 Bad Request` | Campo obrigatório ausente ou valor inválido |
| `404 Not Found` | Cupom não encontrado |
| `422 Unprocessable Entity` | Violação de regra de negócio |

---

## Rodando os testes

**Linux/macOS:**
```bash
./mvnw test
```

**Windows:**
```bash
mvnw.cmd test
```

Resultado esperado:
```
Tests run: 36, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
