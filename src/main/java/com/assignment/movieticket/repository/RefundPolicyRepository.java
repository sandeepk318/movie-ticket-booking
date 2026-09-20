package com.assignment.movieticket.repository;

import com.assignment.movieticket.domain.RefundPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, Long> {
    List<RefundPolicy> findByActiveTrueOrderByMinHoursBeforeShowDesc();
}
