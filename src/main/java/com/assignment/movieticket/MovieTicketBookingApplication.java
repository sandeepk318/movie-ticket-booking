package com.assignment.movieticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MovieTicketBookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(MovieTicketBookingApplication.class, args);
    }
}
