package com.assignment.movieticket.repository;

import com.assignment.movieticket.domain.ShowSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    List<ShowSeat> findByShowId(Long showId);

    /**
     * Row-locks the requested seats for the given show, ordered by seat id so every caller
     * acquires locks in the same order (deadlock-free). Callers must run inside a transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select ss from ShowSeat ss
           where ss.show.id = :showId and ss.seat.id in :seatIds
           order by ss.seat.id asc
           """)
    List<ShowSeat> lockByShowAndSeatIds(Long showId, List<Long> seatIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select ss from ShowSeat ss
           where ss.holdId = :holdId
           order by ss.seat.id asc
           """)
    List<ShowSeat> lockByHoldId(Long holdId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select ss from ShowSeat ss
           where ss.id in :ids
           order by ss.id asc
           """)
    List<ShowSeat> lockByIds(List<Long> ids);
}
