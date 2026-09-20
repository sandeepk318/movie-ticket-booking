package com.assignment.movieticket.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "refund_policy")
@Getter
@Setter
@NoArgsConstructor
public class RefundPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "min_hours_before_show", nullable = false)
    private Integer minHoursBeforeShow;

    @Column(name = "refund_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal refundPercent;

    @Column(nullable = false)
    private boolean active = true;

    public RefundPolicy(String name, Integer minHoursBeforeShow, BigDecimal refundPercent) {
        this.name = name;
        this.minHoursBeforeShow = minHoursBeforeShow;
        this.refundPercent = refundPercent;
    }
}
