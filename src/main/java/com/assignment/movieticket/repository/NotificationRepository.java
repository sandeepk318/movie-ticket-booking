package com.assignment.movieticket.repository;

import com.assignment.movieticket.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
