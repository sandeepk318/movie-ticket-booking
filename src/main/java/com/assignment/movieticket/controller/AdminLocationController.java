package com.assignment.movieticket.controller;

import com.assignment.movieticket.dto.LocationDtos.*;
import com.assignment.movieticket.service.LocationService;
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
public class AdminLocationController {

    private final LocationService locationService;

    @PostMapping("/cities")
    public ResponseEntity<CityResponse> createCity(@Valid @RequestBody CityRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.createCity(req));
    }

    @GetMapping("/cities")
    public List<CityResponse> listCities() {
        return locationService.listCities();
    }

    @PostMapping("/theaters")
    public ResponseEntity<TheaterResponse> createTheater(@Valid @RequestBody TheaterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.createTheater(req));
    }

    @GetMapping("/theaters")
    public List<TheaterResponse> listTheaters(@RequestParam(required = false) Long cityId) {
        return locationService.listTheaters(cityId);
    }

    @PostMapping("/screens")
    public ResponseEntity<ScreenResponse> createScreen(@Valid @RequestBody ScreenRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.createScreen(req));
    }

    @GetMapping("/screens")
    public List<ScreenResponse> listScreens(@RequestParam(required = false) Long theaterId) {
        return locationService.listScreens(theaterId);
    }

    @PutMapping("/screens/{screenId}/seats")
    public List<SeatResponse> setSeatLayout(@PathVariable Long screenId, @Valid @RequestBody SeatLayoutRequest req) {
        return locationService.setSeatLayout(screenId, req);
    }

    @GetMapping("/screens/{screenId}/seats")
    public List<SeatResponse> listSeats(@PathVariable Long screenId) {
        return locationService.listSeats(screenId);
    }
}
