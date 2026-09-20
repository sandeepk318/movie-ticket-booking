package com.assignment.movieticket.service;

import com.assignment.movieticket.domain.RefundPolicy;
import com.assignment.movieticket.dto.RefundDtos.RefundPolicyRequest;
import com.assignment.movieticket.dto.RefundDtos.RefundPolicyResponse;
import com.assignment.movieticket.repository.RefundPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefundPolicyService {

    private final RefundPolicyRepository refundPolicyRepository;

    @Transactional
    public RefundPolicyResponse create(RefundPolicyRequest req) {
        var policy = new RefundPolicy(req.name(), req.minHoursBeforeShow(), req.refundPercent());
        return toResponse(refundPolicyRepository.save(policy));
    }

    @Transactional(readOnly = true)
    public List<RefundPolicyResponse> list() {
        return refundPolicyRepository.findAll().stream().map(this::toResponse).toList();
    }

    /** First active policy (by descending min-hours threshold) whose threshold the booking still meets. */
    @Transactional(readOnly = true)
    public Optional<RefundPolicy> resolve(LocalDateTime showTime, LocalDateTime now) {
        long hoursBefore = Duration.between(now, showTime).toHours();
        return refundPolicyRepository.findByActiveTrueOrderByMinHoursBeforeShowDesc().stream()
                .filter(p -> hoursBefore >= p.getMinHoursBeforeShow())
                .findFirst();
    }

    public BigDecimal percentFor(Optional<RefundPolicy> policy) {
        return policy.map(RefundPolicy::getRefundPercent).orElse(BigDecimal.ZERO);
    }

    private RefundPolicyResponse toResponse(RefundPolicy p) {
        return new RefundPolicyResponse(p.getId(), p.getName(), p.getMinHoursBeforeShow(), p.getRefundPercent(), p.isActive());
    }
}
