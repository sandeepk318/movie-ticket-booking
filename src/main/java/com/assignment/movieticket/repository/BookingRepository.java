package com.assignment.movieticket.repository;

import com.assignment.movieticket.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerIdOrderByBookedAtDesc(Long customerId);

    @Query("""
           select b from Booking b
           where b.status = 'CONFIRMED'
             and b.show.showTime between :from and :to
             and not exists (select 1 from Notification n where n.booking = b and n.type = 'REMINDER')
           """)
    List<Booking> findConfirmedNeedingReminder(LocalDateTime from, LocalDateTime to);
}
