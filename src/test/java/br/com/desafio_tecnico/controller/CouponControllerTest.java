package br.com.desafio_tecnico.controller;

import br.com.desafio_tecnico.entity.Coupon;
import br.com.desafio_tecnico.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class CouponControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CouponRepository couponRepository;

    private MockMvc mockMvc;

    private static final String BASE_URL = "/coupon";
    private static final String FUTURE_DATE = "2030-12-31T00:00:00.000Z";
    private static final String PAST_DATE = "2020-01-01T00:00:00.000Z";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        couponRepository.deleteAll();
    }

    @Test
    void createCoupon_withValidData_returns201() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildRequest("ABCDEF", "Test coupon", "10.0", FUTURE_DATE, null)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.code").value("ABCDEF"))
            .andExpect(jsonPath("$.description").value("Test coupon"))
            .andExpect(jsonPath("$.discountValue").value(10.0))
            .andExpect(jsonPath("$.published").value(false))
            .andExpect(jsonPath("$.redeemed").value(false))
            .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void createCoupon_withPublishedTrue_returnsActiveStatus() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "code": "ABCDEF",
                        "description": "Test coupon",
                        "discountValue": 10.0,
                        "expirationDate": "2030-12-31T00:00:00.000Z",
                        "published": true
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.published").value(true))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createCoupon_withSpecialCharsInCode_sanitizesCodeInResponse() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildRequest("a!b#cd12", "Test coupon", "10.0", FUTURE_DATE, null)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("ABCD12"));
    }

    @Test
    void createCoupon_withCodeTooShortAfterSanitization_returns422() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildRequest("AB!CD", "Test coupon", "10.0", FUTURE_DATE, null)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createCoupon_withDiscountValueBelowMinimum_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildRequest("ABCDEF", "Test coupon", "0.4", FUTURE_DATE, null)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createCoupon_withDiscountValueAtMinimum_returns201() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildRequest("ABCDEF", "Test coupon", "0.5", FUTURE_DATE, null)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.discountValue").value(0.5));
    }

    @Test
    void createCoupon_withPastExpirationDate_returns422() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildRequest("ABCDEF", "Test coupon", "10.0", PAST_DATE, null)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createCoupon_withMissingCode_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Test coupon",
                        "discountValue": 10.0,
                        "expirationDate": "2030-12-31T00:00:00.000Z"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void createCoupon_withDuplicateCode_returns422() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildRequest("ABCDEF", "First coupon", "10.0", FUTURE_DATE, null)))
            .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildRequest("ABCDEF", "Second coupon", "5.0", FUTURE_DATE, null)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createCoupon_withDuplicateCodeDifferentCase_returns422() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildRequest("abcdef", "First coupon", "10.0", FUTURE_DATE, null)))
            .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildRequest("ABCDEF", "Second coupon", "5.0", FUTURE_DATE, null)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createCoupon_withInvalidDateFormat_returns422() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildRequest("ABCDEF", "Test coupon", "10.0", "not-a-date", null)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void findAllCoupons_returnsListWith200() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildRequest("ABCDEF", "Test coupon", "10.0", FUTURE_DATE, null)));

        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].code").value("ABCDEF"));
    }

    @Test
    void findAllCoupons_whenEmpty_returnsEmptyList() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void deleteCoupon_withValidId_returns204() throws Exception {
        Coupon coupon = couponRepository.save(
            Coupon.create("ABCDEF", "Test", new BigDecimal("1.0"), LocalDateTime.now().plusDays(30), false)
        );
        couponRepository.flush();

        mockMvc.perform(delete(BASE_URL + "/" + coupon.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteCoupon_softDelete_preservesDataInGetResponse() throws Exception {
        Coupon coupon = couponRepository.save(
            Coupon.create("ABCDEF", "Test", new BigDecimal("1.0"), LocalDateTime.now().plusDays(30), true)
        );
        couponRepository.flush();

        mockMvc.perform(delete(BASE_URL + "/" + coupon.getId()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].id").value(coupon.getId().toString()))
            .andExpect(jsonPath("$[0].code").value("ABCDEF"))
            .andExpect(jsonPath("$[0].status").value("DELETED"))
            .andExpect(jsonPath("$[0].description").value("Test"))
            .andExpect(jsonPath("$[0].discountValue").value(1.0));
    }

    @Test
    void deleteCoupon_withNonExistentId_returns404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + UUID.randomUUID()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deleteCoupon_whenAlreadyDeleted_returns422() throws Exception {
        Coupon coupon = couponRepository.save(
            Coupon.create("ABCDEF", "Test", new BigDecimal("1.0"), LocalDateTime.now().plusDays(30), false)
        );
        couponRepository.flush();

        mockMvc.perform(delete(BASE_URL + "/" + coupon.getId()))
            .andExpect(status().isNoContent());

        mockMvc.perform(delete(BASE_URL + "/" + coupon.getId()))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").exists());
    }

    private String buildRequest(String code, String description, String discountValue,
                                String expirationDate, Boolean published) {
        String publishedPart = published != null ? ", \"published\": " + published : "";
        return String.format("""
            {
                "code": "%s",
                "description": "%s",
                "discountValue": %s,
                "expirationDate": "%s"%s
            }
            """, code, description, discountValue, expirationDate, publishedPart);
    }
}
