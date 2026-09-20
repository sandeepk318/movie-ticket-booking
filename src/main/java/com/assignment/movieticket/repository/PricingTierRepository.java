package com.assignment.movieticket.repository;

import com.assignment.movieticket.domain.PricingTier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingTierRepository extends JpaRepository<PricingTier, Long> {
}
