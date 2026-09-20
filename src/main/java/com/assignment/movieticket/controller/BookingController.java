package com.assignment.movieticket.controller;

import com.assignment.movieticket.dto.BookingDtos.BookingResponse;
import com.assignment.movieticket.security.CurrentUserService;
import com.assignment.movieticket.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class BookingController {

    private final BookingService bookingService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public List<BookingResponse> listMine() {
        return bookingService.listMine(currentUserService.get());
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable Long id) {
        return bookingService.get(id, currentUserService.get());
    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id) {
        return bookingService.cancel(id, currentUserService.get());
    }
}
