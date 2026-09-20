package com.assignment.movieticket.service;

import com.assignment.movieticket.domain.*;
import com.assignment.movieticket.dto.ShowDtos.*;
import com.assignment.movieticket.exception.ApiExceptions;
import com.assignment.movieticket.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final PricingTierRepository pricingTierRepository;
    private final SeatRepository seatRepository;
    private final PricingService pricingService;

    /** Creating a show snapshots the screen's current seat layout into per-show ShowSeat rows. */
    @Transactional
    public ShowResponse createShow(ShowRequest req) {
        Movie movie = movieRepository.findById(req.movieId())
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Movie not found: " + req.movieId()));
        Screen screen = screenRepository.findById(req.screenId())
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Screen not found: " + req.screenId()));
        PricingTier tier = pricingTierRepository.findById(req.pricingTierId())
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Pricing tier not found: " + req.pricingTierId()));
        if (req.showTime().isBefore(LocalDateTime.now())) {
            throw new ApiExceptions.ValidationException("Show time must be in the future");
        }

        Show show = showRepository.save(new Show(movie, screen, tier, req.showTime()));

        List<Seat> seats = seatRepository.findByScreenId(screen.getId());
        if (seats.isEmpty()) {
            throw new ApiExceptions.ValidationException("Screen has no seat layout defined: " + screen.getId());
        }
        List<ShowSeat> showSeats = seats.stream().map(seat -> new ShowSeat(show, seat)).toList();
        showSeatRepository.saveAll(showSeats);

        return toResponse(show);
    }

    @Transactional(readOnly = true)
    public List<ShowResponse> search(Long cityId, Long movieId, LocalDate date) {
        LocalDateTime from = date != null ? date.atStartOfDay() : null;
        LocalDateTime to = date != null ? date.plusDays(1).atStartOfDay() : null;
        return showRepository.search(cityId, movieId, from, to).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ShowResponse getShow(Long id) {
        return toResponse(getShowEntity(id));
    }

    @Transactional(readOnly = true)
    public Show getShowEntity(Long id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Show not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ShowSeatResponse> getSeatMap(Long showId) {
        Show show = getShowEntity(showId);
        return showSeatRepository.findByShowId(showId).stream()
                .map(ss -> new ShowSeatResponse(ss.getId(), ss.getSeat().getId(), ss.getSeat().label(),
                        ss.getSeat().getSeatType(), ss.getStatus(), pricingService.priceFor(show, ss.getSeat().getSeatType())))
                .toList();
    }

    private ShowResponse toResponse(Show s) {
        return new ShowResponse(s.getId(), s.getMovie().getId(), s.getMovie().getTitle(), s.getScreen().getId(),
                s.getScreen().getTheater().getName(), s.getScreen().getTheater().getCity().getId(),
                s.getScreen().getTheater().getCity().getName(), s.getShowTime(), s.getStatus());
    }
}
