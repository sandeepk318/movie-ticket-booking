package com.assignment.movieticket.service;

import com.assignment.movieticket.domain.DiscountCode;
import com.assignment.movieticket.dto.DiscountDtos.DiscountCodeRequest;
import com.assignment.movieticket.dto.DiscountDtos.DiscountCodeResponse;
import com.assignment.movieticket.exception.ApiExceptions;
import com.assignment.movieticket.repository.DiscountCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountCodeService {

    private final DiscountCodeRepository discountCodeRepository;

    @Transactional
    public DiscountCodeResponse create(DiscountCodeRequest req) {
        var code = new DiscountCode(req.code().toUpperCase(), req.discountType(), req.value(), req.maxUsage(),
                req.validFrom(), req.validTo());
        return toResponse(discountCodeRepository.save(code));
    }

    @Transactional(readOnly = true)
    public List<DiscountCodeResponse> list() {
        return discountCodeRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Locks the discount code row, validates it's usable right now, atomically bumps its usage
     * counter (preventing over-redemption under concurrent confirms), and returns the discount
     * amount to apply on top of {@code baseAmount}. Runs inside the caller's transaction
     * (booking confirmation) so a failed confirm rolls the usage bump back too.
     */
    @Transactional
    public AppliedDiscount applyAndLock(String rawCode, BigDecimal baseAmount, LocalDateTime now) {
        var code = discountCodeRepository.lockByCode(rawCode.toUpperCase())
                .orElseThrow(() -> new ApiExceptions.ValidationException("Unknown discount code: " + rawCode));
        if (!code.isUsable(now)) {
            throw new ApiExceptions.ValidationException("Discount code is not currently usable: " + rawCode);
        }
        BigDecimal discountAmount = code.getDiscountType() == DiscountCode.DiscountType.PERCENT
                ? baseAmount.multiply(code.getValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : code.getValue();
        if (discountAmount.compareTo(baseAmount) > 0) {
            discountAmount = baseAmount;
        }
        code.setUsageCount(code.getUsageCount() + 1);
        return new AppliedDiscount(code, discountAmount);
    }

    private DiscountCodeResponse toResponse(DiscountCode c) {
        return new DiscountCodeResponse(c.getId(), c.getCode(), c.getDiscountType(), c.getValue(), c.getMaxUsage(),
                c.getUsageCount(), c.getValidFrom(), c.getValidTo(), c.isActive());
    }

    public record AppliedDiscount(DiscountCode discountCode, BigDecimal discountAmount) {}
}
