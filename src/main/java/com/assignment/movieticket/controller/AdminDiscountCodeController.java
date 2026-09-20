package com.assignment.movieticket.controller;

import com.assignment.movieticket.dto.DiscountDtos.DiscountCodeRequest;
import com.assignment.movieticket.dto.DiscountDtos.DiscountCodeResponse;
import com.assignment.movieticket.service.DiscountCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/discount-codes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDiscountCodeController {

    private final DiscountCodeService discountCodeService;

    @PostMapping
    public ResponseEntity<DiscountCodeResponse> create(@Valid @RequestBody DiscountCodeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(discountCodeService.create(req));
    }

    @GetMapping
    public List<DiscountCodeResponse> list() {
        return discountCodeService.list();
    }
}
