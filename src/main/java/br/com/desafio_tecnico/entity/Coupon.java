package br.com.desafio_tecnico.entity;

import br.com.desafio_tecnico.enums.CouponStatus;
import br.com.desafio_tecnico.exception.BusinessException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "coupons")
public class Coupon {

    private static final int CODE_LENGTH = 6;
    private static final BigDecimal MIN_DISCOUNT = new BigDecimal("0.5");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 6, unique = true)
    private String code;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal discountValue;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    @Column(nullable = false)
    private boolean published;

    @Column(nullable = false)
    private boolean redeemed = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus status;

    protected Coupon() {}

    public static Coupon create(String rawCode, String description, BigDecimal discountValue,
                                LocalDateTime expirationDate, boolean published) {
        String code = sanitizeCode(rawCode);

        if (code.length() != CODE_LENGTH) {
            throw new BusinessException(
                "O código deve ter exatamente 6 caracteres alfanuméricos após a remoção de caracteres especiais"
            );
        }

        if (discountValue.compareTo(MIN_DISCOUNT) < 0) {
            throw new BusinessException("O valor do desconto deve ser no mínimo 0.5");
        }

        if (expirationDate.isBefore(LocalDateTime.now())) {
            throw new BusinessException("A data de expiração não pode ser no passado");
        }

        Coupon coupon = new Coupon();
        coupon.code = code;
        coupon.description = description;
        coupon.discountValue = discountValue;
        coupon.expirationDate = expirationDate;
        coupon.published = published;
        coupon.redeemed = false;
        coupon.status = published ? CouponStatus.ACTIVE : CouponStatus.INACTIVE;
        return coupon;
    }

    public void softDelete() {
        if (this.status == CouponStatus.DELETED) {
            throw new BusinessException("O cupom já foi deletado");
        }
        this.status = CouponStatus.DELETED;
    }

    public static String sanitizeCode(String rawCode) {
        return rawCode.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public LocalDateTime getExpirationDate() { return expirationDate; }
    public boolean isPublished() { return published; }
    public boolean isRedeemed() { return redeemed; }
    public CouponStatus getStatus() { return status; }
}
