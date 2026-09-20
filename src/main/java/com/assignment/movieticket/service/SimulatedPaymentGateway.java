package com.assignment.movieticket.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Deterministic simulated gateway: succeeds unless the amount is non-positive or the caller
 * passes method "FAIL_TEST" (used by tests to exercise the payment-failure path).
 */
@Service
public class SimulatedPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResult charge(BigDecimal amount, String method) {
        if (amount == null || amount.signum() < 0) {
            return new PaymentResult(false, null, "Invalid amount");
        }
        if ("FAIL_TEST".equalsIgnoreCase(method)) {
            return new PaymentResult(false, null, "Simulated payment decline");
        }
        return new PaymentResult(true, "TXN-" + UUID.randomUUID(), "Approved");
    }
}
