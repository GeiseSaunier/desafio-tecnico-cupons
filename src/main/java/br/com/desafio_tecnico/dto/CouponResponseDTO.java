package br.com.desafio_tecnico.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class CouponResponseDTO {

    private UUID id;
    private String code;
    private String description;
    private BigDecimal discountValue;
    private String expirationDate;
    private boolean published;
    private boolean redeemed;
    private String status;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public boolean isRedeemed() { return redeemed; }
    public void setRedeemed(boolean redeemed) { this.redeemed = redeemed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}