package com.assignment.movieticket.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movie")
@Getter
@Setter
@NoArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(length = 50)
    private String language;

    @Column(length = 50)
    private String genre;

    public Movie(String title, Integer durationMinutes, String language, String genre) {
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.language = language;
        this.genre = genre;
    }
}
