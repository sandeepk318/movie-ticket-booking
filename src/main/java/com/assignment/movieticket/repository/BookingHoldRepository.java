package com.assignment.movieticket.repository;

import com.assignment.movieticket.domain.BookingHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingHoldRepository extends JpaRepository<BookingHold, Long> {

    @Query("select h from BookingHold h where h.status = 'ACTIVE' and h.expiresAt < :now")
    List<BookingHold> findExpiredActiveHolds(LocalDateTime now);
}
