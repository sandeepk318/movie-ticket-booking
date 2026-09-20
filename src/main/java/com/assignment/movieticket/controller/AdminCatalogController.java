package com.assignment.movieticket.controller;

import com.assignment.movieticket.dto.CatalogDtos.*;
import com.assignment.movieticket.service.CatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCatalogController {

    private final CatalogService catalogService;

    @PostMapping("/movies")
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createMovie(req));
    }

    @GetMapping("/movies")
    public List<MovieResponse> listMovies() {
        return catalogService.listMovies();
    }

    @PostMapping("/pricing-tiers")
    public ResponseEntity<PricingTierResponse> createPricingTier(@Valid @RequestBody PricingTierRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createPricingTier(req));
    }

    @GetMapping("/pricing-tiers")
    public List<PricingTierResponse> listPricingTiers() {
        return catalogService.listPricingTiers();
    }
}
