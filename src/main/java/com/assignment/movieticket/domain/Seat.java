package com.assignment.movieticket.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "seat", uniqueConstraints = @UniqueConstraint(columnNames = {"screen_id", "seat_row", "seat_number"}))
@Getter
@Setter
@NoArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(name = "seat_row", nullable = false, length = 4)
    private String seatRow;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false, length = 16)
    private SeatType seatType;

    public Seat(Screen screen, String seatRow, Integer seatNumber, SeatType seatType) {
        this.screen = screen;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
    }

    public String label() {
        return seatRow + seatNumber;
    }
}
