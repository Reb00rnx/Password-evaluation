package com.uniportal.passwordgym.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestDto(

        @NotBlank(message = "Username cant be blank")
        String username,
        @NotBlank(message = "Email cant be blank")
        String email,
        @NotBlank(message = "Password cant be blank")
        @Size(max = 128, message = "Password must be at most 128 characters")
        String password
) {}
