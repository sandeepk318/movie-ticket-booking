package com.assignment.movieticket.security;

import com.assignment.movieticket.domain.AppUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public AppUser get() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof AppUserDetails details) {
            return details.getUser();
        }
        throw new IllegalStateException("No authenticated user in context");
    }
}
