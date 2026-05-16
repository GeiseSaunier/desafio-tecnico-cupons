package br.com.desafio_tecnico.domain;

import br.com.desafio_tecnico.entity.Coupon;
import br.com.desafio_tecnico.enums.CouponStatus;
import br.com.desafio_tecnico.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CouponDomainTest {

    private static final LocalDateTime FUTURE_DATE = LocalDateTime.now().plusDays(30);
    private static final LocalDateTime PAST_DATE = LocalDateTime.now().minusDays(1);

    @Test
    void create_withValidData_returnsInactiveCoupon() {
        Coupon coupon = Coupon.create("ABCDEF", "desc", new BigDecimal("1.0"), FUTURE_DATE, false);

        assertEquals("ABCDEF", coupon.getCode());
        assertEquals("desc", coupon.getDescription());
        assertEquals(new BigDecimal("1.0"), coupon.getDiscountValue());
        assertFalse(coupon.isPublished());
        assertFalse(coupon.isRedeemed());
        assertEquals(CouponStatus.INACTIVE, coupon.getStatus());
    }

    @Test
    void create_withPublishedTrue_returnsActiveCoupon() {
        Coupon coupon = Coupon.create("ABCDEF", "desc", new BigDecimal("1.0"), FUTURE_DATE, true);

        assertTrue(coupon.isPublished());
        assertEquals(CouponStatus.ACTIVE, coupon.getStatus());
    }

    @Test
    void create_withSpecialCharsResultingInSixAlphanumericChars_sanitizesAndSucceeds() {
        Coupon coupon = Coupon.create("A!B#cdef", "desc", new BigDecimal("1.0"), FUTURE_DATE, false);

        assertEquals("ABCDEF", coupon.getCode());
    }

    @Test
    void create_withHyphensInCode_sanitizesAndSucceeds() {
        Coupon coupon = Coupon.create("ab-cd-ef", "desc", new BigDecimal("1.0"), FUTURE_DATE, false);

        assertEquals("ABCDEF", coupon.getCode());
    }

    @Test
    void create_withCodeTooShortAfterSanitization_throwsBusinessException() {
        BusinessException ex = assertThrows(BusinessException.class,
            () -> Coupon.create("AB!CD", "desc", new BigDecimal("1.0"), FUTURE_DATE, false));

        assertTrue(ex.getMessage().contains("6 caracteres alfanuméricos"));
    }

    @Test
    void create_withOnlySpecialCharsInCode_throwsBusinessException() {
        assertThrows(BusinessException.class,
            () -> Coupon.create("!@#$%^", "desc", new BigDecimal("1.0"), FUTURE_DATE, false));
    }

    @Test
    void create_withCodeTooLongAfterSanitization_throwsBusinessException() {
        assertThrows(BusinessException.class,
            () -> Coupon.create("ABCDEFG", "desc", new BigDecimal("1.0"), FUTURE_DATE, false));
    }

    @Test
    void create_withDiscountValueAtMinimum_succeeds() {
        Coupon coupon = Coupon.create("ABCDEF", "desc", new BigDecimal("0.5"), FUTURE_DATE, false);

        assertEquals(new BigDecimal("0.5"), coupon.getDiscountValue());
    }

    @Test
    void create_withDiscountValueBelowMinimum_throwsBusinessException() {
        BusinessException ex = assertThrows(BusinessException.class,
            () -> Coupon.create("ABCDEF", "desc", new BigDecimal("0.4"), FUTURE_DATE, false));

        assertTrue(ex.getMessage().contains("0.5"));
    }

    @Test
    void create_withZeroDiscountValue_throwsBusinessException() {
        assertThrows(BusinessException.class,
            () -> Coupon.create("ABCDEF", "desc", BigDecimal.ZERO, FUTURE_DATE, false));
    }

    @Test
    void create_withExpirationDateInPast_throwsBusinessException() {
        BusinessException ex = assertThrows(BusinessException.class,
            () -> Coupon.create("ABCDEF", "desc", new BigDecimal("1.0"), PAST_DATE, false));

        assertTrue(ex.getMessage().contains("passado"));
    }

    @Test
    void softDelete_whenActive_setsStatusToDeleted() {
        Coupon coupon = Coupon.create("ABCDEF", "desc", new BigDecimal("1.0"), FUTURE_DATE, true);

        coupon.softDelete();

        assertEquals(CouponStatus.DELETED, coupon.getStatus());
    }

    @Test
    void softDelete_whenInactive_setsStatusToDeleted() {
        Coupon coupon = Coupon.create("ABCDEF", "desc", new BigDecimal("1.0"), FUTURE_DATE, false);

        coupon.softDelete();

        assertEquals(CouponStatus.DELETED, coupon.getStatus());
    }

    @Test
    void softDelete_whenAlreadyDeleted_throwsBusinessException() {
        Coupon coupon = Coupon.create("ABCDEF", "desc", new BigDecimal("1.0"), FUTURE_DATE, true);
        coupon.softDelete();

        BusinessException ex = assertThrows(BusinessException.class, coupon::softDelete);

        assertTrue(ex.getMessage().contains("já foi deletado"));
    }

    @Test
    void sanitizeCode_removesAllSpecialChars() {
        assertEquals("ABC123", Coupon.sanitizeCode("A!B@C#1$2%3^"));
    }

    @Test
    void sanitizeCode_withLowercase_normalizesToUppercase() {
        assertEquals("ABCDEF", Coupon.sanitizeCode("abcdef"));
    }

    @Test
    void sanitizeCode_withPureAlphanumeric_returnsUnchanged() {
        assertEquals("ABCDEF", Coupon.sanitizeCode("ABCDEF"));
    }

    @Test
    void sanitizeCode_withHyphensAndSpaces_removesAll() {
        assertEquals("ABCDEF", Coupon.sanitizeCode("abc-def"));
    }
}
