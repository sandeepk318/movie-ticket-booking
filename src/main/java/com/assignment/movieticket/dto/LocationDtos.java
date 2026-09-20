package com.assignment.movieticket.dto;

import com.assignment.movieticket.domain.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;

import java.util.List;

public class LocationDtos {

    public record CityRequest(@NotBlank String name) {}
    public record CityResponse(Long id, String name) {}

    public record TheaterRequest(@NotNull Long cityId, @NotBlank String name, String address) {}
    public record TheaterResponse(Long id, Long cityId, String cityName, String name, String address) {}

    public record ScreenRequest(@NotNull Long theaterId, @NotBlank String name) {}
    public record ScreenResponse(Long id, Long theaterId, String name, int seatCount) {}

    public record SeatSpec(@NotBlank String seatRow, @Min(1) int seatNumber, @NotNull SeatType seatType) {}
    public record SeatLayoutRequest(@NotNull @Valid List<SeatSpec> seats) {}
    public record SeatResponse(Long id, String seatRow, int seatNumber, SeatType seatType, String label) {}
}
