package com.assignment.movieticket.controller;

import com.assignment.movieticket.dto.ShowDtos.ShowRequest;
import com.assignment.movieticket.dto.ShowDtos.ShowResponse;
import com.assignment.movieticket.service.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/shows")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminShowController {

    private final ShowService showService;

    @PostMapping
    public ResponseEntity<ShowResponse> createShow(@Valid @RequestBody ShowRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(showService.createShow(req));
    }
}
