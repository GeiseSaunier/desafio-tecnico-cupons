package br.com.desafio_tecnico.service;

import br.com.desafio_tecnico.dto.CouponRequestDTO;
import br.com.desafio_tecnico.dto.CouponResponseDTO;
import br.com.desafio_tecnico.entity.Coupon;
import br.com.desafio_tecnico.exception.BusinessException;
import br.com.desafio_tecnico.exception.ResourceNotFoundException;
import br.com.desafio_tecnico.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CouponService {

    private static final DateTimeFormatter RESPONSE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public CouponResponseDTO create(CouponRequestDTO request) {
        String sanitizedCode = Coupon.sanitizeCode(request.getCode());

        if (couponRepository.existsByCode(sanitizedCode)) {
            throw new BusinessException("Já existe um cupom com o código '" + sanitizedCode + "'");
        }

        LocalDateTime expirationDate = parseExpirationDate(request.getExpirationDate());
        boolean published = Boolean.TRUE.equals(request.getPublished());

        Coupon coupon = Coupon.create(
            request.getCode(),
            request.getDescription(),
            request.getDiscountValue(),
            expirationDate,
            published
        );

        return toResponse(couponRepository.save(coupon));
    }

    @Transactional(readOnly = true)
    public List<CouponResponseDTO> findAll() {
        return couponRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public void delete(UUID id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cupom não encontrado com o id: " + id));

        coupon.softDelete();
        couponRepository.save(coupon);
    }

    private LocalDateTime parseExpirationDate(String dateStr) {
        try {
            return OffsetDateTime.parse(dateStr).toLocalDateTime();
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(dateStr);
            } catch (DateTimeParseException e2) {
                throw new BusinessException(
                    "Formato de data inválido. Use ISO 8601 (ex: 2025-10-30T00:00:00.000Z)"
                );
            }
        }
    }

    private CouponResponseDTO toResponse(Coupon coupon) {
        CouponResponseDTO response = new CouponResponseDTO();
        response.setId(coupon.getId());
        response.setCode(coupon.getCode());
        response.setDescription(coupon.getDescription());
        response.setDiscountValue(coupon.getDiscountValue().setScale(2, RoundingMode.HALF_UP));
        response.setExpirationDate(coupon.getExpirationDate().format(RESPONSE_FORMATTER));
        response.setPublished(coupon.isPublished());
        response.setRedeemed(coupon.isRedeemed());
        response.setStatus(coupon.getStatus().name());
        return response;
    }
}
