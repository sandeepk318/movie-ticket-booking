package com.assignment.movieticket.service;

import com.assignment.movieticket.domain.Movie;
import com.assignment.movieticket.domain.PricingTier;
import com.assignment.movieticket.dto.CatalogDtos.*;
import com.assignment.movieticket.repository.MovieRepository;
import com.assignment.movieticket.repository.PricingTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final MovieRepository movieRepository;
    private final PricingTierRepository pricingTierRepository;

    @Transactional
    public MovieResponse createMovie(MovieRequest req) {
        Movie movie = new Movie(req.title(), req.durationMinutes(), req.language(), req.genre());
        return toResponse(movieRepository.save(movie));
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> listMovies() {
        return movieRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public PricingTierResponse createPricingTier(PricingTierRequest req) {
        PricingTier tier = new PricingTier(req.name(), req.regularSeatPrice(), req.premiumSeatPrice(), req.weekendSurchargePercent());
        return toResponse(pricingTierRepository.save(tier));
    }

    @Transactional(readOnly = true)
    public List<PricingTierResponse> listPricingTiers() {
        return pricingTierRepository.findAll().stream().map(this::toResponse).toList();
    }

    private MovieResponse toResponse(Movie m) {
        return new MovieResponse(m.getId(), m.getTitle(), m.getDurationMinutes(), m.getLanguage(), m.getGenre());
    }

    private PricingTierResponse toResponse(PricingTier t) {
        return new PricingTierResponse(t.getId(), t.getName(), t.getRegularSeatPrice(), t.getPremiumSeatPrice(), t.getWeekendSurchargePercent());
    }
}
