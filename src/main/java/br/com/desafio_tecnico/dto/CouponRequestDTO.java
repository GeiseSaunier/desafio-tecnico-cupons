package br.com.desafio_tecnico.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CouponRequestDTO {

    @NotBlank(message = "o código é obrigatório")
    private String code;

    @NotBlank(message = "a descrição é obrigatória")
    private String description;

    @NotNull(message = "o valor do desconto é obrigatório")
    @DecimalMin(value = "0.5", message = "o valor do desconto deve ser no mínimo 0.5")
    private BigDecimal discountValue;

    @NotBlank(message = "a data de expiração é obrigatória")
    private String expirationDate;

    private Boolean published = false;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public Boolean getPublished() { return published; }
    public void setPublished(Boolean published) { this.published = published; }
}