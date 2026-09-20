package com.assignment.movieticket.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "show")
@Getter
@Setter
@NoArgsConstructor
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pricing_tier_id", nullable = false)
    private PricingTier pricingTier;

    @Column(name = "show_time", nullable = false)
    private LocalDateTime showTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ShowStatus status = ShowStatus.SCHEDULED;

    public enum ShowStatus { SCHEDULED, CANCELLED, COMPLETED }

    public Show(Movie movie, Screen screen, PricingTier pricingTier, LocalDateTime showTime) {
        this.movie = movie;
        this.screen = screen;
        this.pricingTier = pricingTier;
        this.showTime = showTime;
    }

    public boolean isWeekend() {
        var day = showTime.getDayOfWeek();
        return day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY;
    }
}
