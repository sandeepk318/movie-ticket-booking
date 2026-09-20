package com.assignment.movieticket.config;

import com.assignment.movieticket.domain.*;
import com.assignment.movieticket.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds a working demo dataset (users + a full venue/show/discount/refund setup) on first boot
 * so the reviewer can hit the API immediately without a manual admin-setup pass.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final CityRepository cityRepository;
    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final MovieRepository movieRepository;
    private final PricingTierRepository pricingTierRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final DiscountCodeRepository discountCodeRepository;
    private final RefundPolicyRepository refundPolicyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (appUserRepository.count() > 0) {
            return;
        }
        log.info("Seeding demo data (first boot)...");

        appUserRepository.save(new AppUser("admin", passwordEncoder.encode("admin123"), "admin@example.com", AppUser.Role.ADMIN));
        AppUser customer = appUserRepository.save(
                new AppUser("customer1", passwordEncoder.encode("customer123"), "customer1@example.com", AppUser.Role.CUSTOMER));

        City bengaluru = cityRepository.save(new City("Bengaluru"));
        Theater theater = theaterRepository.save(new Theater(bengaluru, "PVR Forum", "Forum Mall, Koramangala"));
        Screen screen = screenRepository.save(new Screen(theater, "Screen 1"));

        List<Seat> seats = new ArrayList<>();
        for (char row = 'A'; row <= 'E'; row++) {
            SeatType type = (row == 'A' || row == 'B') ? SeatType.PREMIUM : SeatType.REGULAR;
            for (int num = 1; num <= 10; num++) {
                seats.add(new Seat(screen, String.valueOf(row), num, type));
            }
        }
        seatRepository.saveAll(seats);

        Movie movie = movieRepository.save(new Movie("Interstellar Returns", 150, "English", "Sci-Fi"));
        PricingTier tier = pricingTierRepository.save(
                new PricingTier("Standard", BigDecimal.valueOf(200), BigDecimal.valueOf(350), BigDecimal.valueOf(20)));

        LocalDateTime showTime = LocalDate.now().plusDays(1).atTime(LocalTime.of(19, 0));
        Show show = showRepository.save(new Show(movie, screen, tier, showTime));
        List<ShowSeat> showSeats = seats.stream().map(seat -> new ShowSeat(show, seat)).toList();
        showSeatRepository.saveAll(showSeats);

        discountCodeRepository.save(new DiscountCode("WELCOME10", DiscountCode.DiscountType.PERCENT, BigDecimal.TEN,
                100, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30)));

        refundPolicyRepository.save(new RefundPolicy("Full refund (24h+)", 24, BigDecimal.valueOf(100)));
        refundPolicyRepository.save(new RefundPolicy("Partial refund (2h+)", 2, BigDecimal.valueOf(50)));
        refundPolicyRepository.save(new RefundPolicy("No refund (<2h)", 0, BigDecimal.ZERO));

        log.info("Seed complete: admin/admin123 (ADMIN), customer1/customer123 (CUSTOMER), show id={}, discount=WELCOME10",
                show.getId());
    }
}
