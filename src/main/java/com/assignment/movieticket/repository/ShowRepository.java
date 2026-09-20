package com.assignment.movieticket.repository;

import com.assignment.movieticket.domain.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("""
           select s from Show s
           where (:cityId is null or s.screen.theater.city.id = :cityId)
             and (:movieId is null or s.movie.id = :movieId)
             and (:from is null or s.showTime >= :from)
             and (:to is null or s.showTime < :to)
           order by s.showTime asc
           """)
    List<Show> search(Long cityId, Long movieId, LocalDateTime from, LocalDateTime to);
}
