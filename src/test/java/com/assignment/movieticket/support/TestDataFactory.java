package com.assignment.movieticket.support;

import com.assignment.movieticket.domain.*;
import com.assignment.movieticket.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Builds fresh, isolated fixtures per test so tests never contend on seeded/shared rows. */
@Component
public class TestDataFactory {

    private final CityRepository cityRepository;
    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final MovieRepository movieRepository;
    private final PricingTierRepository pricingTierRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataFactory(CityRepository cityRepository, TheaterRepository theaterRepository,
                            ScreenRepository screenRepository, SeatRepository seatRepository,
                            MovieRepository movieRepository, PricingTierRepository pricingTierRepository,
                            ShowRepository showRepository, ShowSeatRepository showSeatRepository,
                            AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.cityRepository = cityRepository;
        this.theaterRepository = theaterRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
        this.movieRepository = movieRepository;
        this.pricingTierRepository = pricingTierRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Show createShowWithSeats(int seatCount, LocalDateTime showTime) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        City city = cityRepository.save(new City("City-" + suffix));
        Theater theater = theaterRepository.save(new Theater(city, "Theater-" + suffix, "addr"));
        Screen screen = screenRepository.save(new Screen(theater, "Screen-" + suffix));
        Movie movie = movieRepository.save(new Movie("Movie-" + suffix, 120, "English", "Drama"));
        PricingTier tier = pricingTierRepository.save(new PricingTier("Tier-" + suffix,
                BigDecimal.valueOf(100), BigDecimal.valueOf(200), BigDecimal.valueOf(10)));

        List<Seat> seats = java.util.stream.IntStream.range(0, seatCount)
                .mapToObj(i -> new Seat(screen, "A", i + 1, SeatType.REGULAR))
                .toList();
        seatRepository.saveAll(seats);

        Show show = showRepository.save(new Show(movie, screen, tier, showTime));
        List<ShowSeat> showSeats = seats.stream().map(s -> new ShowSeat(show, s)).toList();
        showSeatRepository.saveAll(showSeats);
        return show;
    }

    @Transactional
    public AppUser createCustomer() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return appUserRepository.save(new AppUser("cust-" + suffix, passwordEncoder.encode("pass1234"),
                "cust-" + suffix + "@example.com", AppUser.Role.CUSTOMER));
    }
}
