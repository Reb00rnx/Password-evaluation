package com.uniportal.passwordgym.Dto;

import jakarta.validation.constraints.NotBlank;

public record RequestDto(

        @NotBlank(message = "Username cant be blank")
        String username,
        @NotBlank(message = "Email cant be blank")
        String email,
        @NotBlank(message = "Password cant be blank")
        String password
) {}
