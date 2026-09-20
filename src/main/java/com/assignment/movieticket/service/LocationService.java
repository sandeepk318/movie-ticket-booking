package com.assignment.movieticket.service;

import com.assignment.movieticket.domain.City;
import com.assignment.movieticket.domain.Screen;
import com.assignment.movieticket.domain.Seat;
import com.assignment.movieticket.domain.Theater;
import com.assignment.movieticket.dto.LocationDtos.*;
import com.assignment.movieticket.exception.ApiExceptions;
import com.assignment.movieticket.repository.CityRepository;
import com.assignment.movieticket.repository.ScreenRepository;
import com.assignment.movieticket.repository.SeatRepository;
import com.assignment.movieticket.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final CityRepository cityRepository;
    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public CityResponse createCity(CityRequest req) {
        return toResponse(cityRepository.save(new City(req.name())));
    }

    @Transactional(readOnly = true)
    public List<CityResponse> listCities() {
        return cityRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public TheaterResponse createTheater(TheaterRequest req) {
        City city = cityRepository.findById(req.cityId())
                .orElseThrow(() -> new ApiExceptions.NotFoundException("City not found: " + req.cityId()));
        Theater theater = theaterRepository.save(new Theater(city, req.name(), req.address()));
        return toResponse(theater);
    }

    @Transactional(readOnly = true)
    public List<TheaterResponse> listTheaters(Long cityId) {
        List<Theater> theaters = cityId == null ? theaterRepository.findAll() : theaterRepository.findByCityId(cityId);
        return theaters.stream().map(this::toResponse).toList();
    }

    @Transactional
    public ScreenResponse createScreen(ScreenRequest req) {
        Theater theater = theaterRepository.findById(req.theaterId())
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Theater not found: " + req.theaterId()));
        Screen screen = screenRepository.save(new Screen(theater, req.name()));
        return toResponse(screen);
    }

    @Transactional(readOnly = true)
    public List<ScreenResponse> listScreens(Long theaterId) {
        List<Screen> screens = theaterId == null ? screenRepository.findAll() : screenRepository.findByTheaterId(theaterId);
        return screens.stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<SeatResponse> setSeatLayout(Long screenId, SeatLayoutRequest req) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Screen not found: " + screenId));
        List<Seat> seats = req.seats().stream()
                .map(spec -> new Seat(screen, spec.seatRow().toUpperCase(), spec.seatNumber(), spec.seatType()))
                .toList();
        return seatRepository.saveAll(seats).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> listSeats(Long screenId) {
        return seatRepository.findByScreenId(screenId).stream().map(this::toResponse).toList();
    }

    private CityResponse toResponse(City c) {
        return new CityResponse(c.getId(), c.getName());
    }

    private TheaterResponse toResponse(Theater t) {
        return new TheaterResponse(t.getId(), t.getCity().getId(), t.getCity().getName(), t.getName(), t.getAddress());
    }

    private ScreenResponse toResponse(Screen s) {
        return new ScreenResponse(s.getId(), s.getTheater().getId(), s.getName(), seatRepository.findByScreenId(s.getId()).size());
    }

    private SeatResponse toResponse(Seat s) {
        return new SeatResponse(s.getId(), s.getSeatRow(), s.getSeatNumber(), s.getSeatType(), s.label());
    }
}
