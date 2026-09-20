package com.assignment.movieticket.controller;

import com.assignment.movieticket.dto.ShowDtos.ShowResponse;
import com.assignment.movieticket.dto.ShowDtos.ShowSeatResponse;
import com.assignment.movieticket.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    @GetMapping
    public List<ShowResponse> search(@RequestParam(required = false) Long cityId,
                                      @RequestParam(required = false) Long movieId,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return showService.search(cityId, movieId, date);
    }

    @GetMapping("/{showId}")
    public ShowResponse get(@PathVariable Long showId) {
        return showService.getShow(showId);
    }

    @GetMapping("/{showId}/seats")
    public List<ShowSeatResponse> seatMap(@PathVariable Long showId) {
        return showService.getSeatMap(showId);
    }
}
