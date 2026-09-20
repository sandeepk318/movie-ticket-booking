package com.assignment.movieticket.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record RegisterRequest(@NotBlank @Size(min = 3, max = 64) String username,
                                   @NotBlank @Size(min = 6) String password,
                                   @NotBlank @Email String email) {}

    public record UserResponse(Long id, String username, String email, String role) {}
}
