package com.assignment.movieticket.service;

import java.math.BigDecimal;

/**
 * Seam for a real payment integration. The take-home scope explicitly excludes external
 * gateway integration, so {@link SimulatedPaymentGateway} stands in for it.
 */
public interface PaymentGateway {

    PaymentResult charge(BigDecimal amount, String method);

    record PaymentResult(boolean success, String transactionRef, String message) {}
}
