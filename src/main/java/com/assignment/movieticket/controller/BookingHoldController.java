package com.assignment.movieticket.controller;

import com.assignment.movieticket.dto.BookingDtos.*;
import com.assignment.movieticket.security.CurrentUserService;
import com.assignment.movieticket.service.BookingHoldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class BookingHoldController {

    private final BookingHoldService bookingHoldService;
    private final CurrentUserService currentUserService;

    @PostMapping("/api/shows/{showId}/hold")
    public ResponseEntity<HoldResponse> hold(@PathVariable Long showId, @Valid @RequestBody HoldRequest req) {
        var response = bookingHoldService.createHold(showId, req, currentUserService.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/api/holds/{holdId}")
    public ResponseEntity<Void> release(@PathVariable Long holdId) {
        bookingHoldService.releaseHold(holdId, currentUserService.get());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/holds/{holdId}/confirm")
    public ResponseEntity<BookingResponse> confirm(@PathVariable Long holdId, @Valid @RequestBody ConfirmRequest req) {
        var response = bookingHoldService.confirm(holdId, req, currentUserService.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
