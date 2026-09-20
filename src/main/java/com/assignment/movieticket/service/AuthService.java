package com.assignment.movieticket.service;

import com.assignment.movieticket.domain.AppUser;
import com.assignment.movieticket.dto.AuthDtos.RegisterRequest;
import com.assignment.movieticket.dto.AuthDtos.UserResponse;
import com.assignment.movieticket.exception.ApiExceptions;
import com.assignment.movieticket.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    /** Self-signup always creates a CUSTOMER; admin accounts are seeded, not self-registered. */
    @Transactional
    public UserResponse register(RegisterRequest req) {
        if (appUserRepository.existsByUsername(req.username())) {
            throw new ApiExceptions.ValidationException("Username already taken: " + req.username());
        }
        AppUser user = new AppUser(req.username(), passwordEncoder.encode(req.password()), req.email(), AppUser.Role.CUSTOMER);
        user = appUserRepository.save(user);
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
    }
}
