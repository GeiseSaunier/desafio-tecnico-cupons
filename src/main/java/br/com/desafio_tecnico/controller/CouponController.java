package br.com.desafio_tecnico.controller;

import br.com.desafio_tecnico.dto.CouponRequestDTO;
import br.com.desafio_tecnico.dto.CouponResponseDTO;
import br.com.desafio_tecnico.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/coupon")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponseDTO create(@Valid @RequestBody CouponRequestDTO request) {
        return couponService.create(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CouponResponseDTO> findAll() {
        return couponService.findAll();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        couponService.delete(id);
    }
}
