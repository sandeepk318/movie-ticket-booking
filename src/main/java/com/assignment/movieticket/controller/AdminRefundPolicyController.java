package com.assignment.movieticket.controller;

import com.assignment.movieticket.dto.RefundDtos.RefundPolicyRequest;
import com.assignment.movieticket.dto.RefundDtos.RefundPolicyResponse;
import com.assignment.movieticket.service.RefundPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/refund-policies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRefundPolicyController {

    private final RefundPolicyService refundPolicyService;

    @PostMapping
    public ResponseEntity<RefundPolicyResponse> create(@Valid @RequestBody RefundPolicyRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(refundPolicyService.create(req));
    }

    @GetMapping
    public List<RefundPolicyResponse> list() {
        return refundPolicyService.list();
    }
}
