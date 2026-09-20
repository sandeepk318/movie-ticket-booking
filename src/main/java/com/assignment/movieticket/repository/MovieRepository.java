package com.assignment.movieticket.repository;

import com.assignment.movieticket.domain.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
