package com.uniportal.passwordgym.Dto;

import com.uniportal.passwordgym.Enum.StrengthScore;

import java.util.List;

public record ResponseDto(
    int score,
    StrengthScore strength,
    boolean meetsPolicy,
    boolean containsUsernameOrEmail ,
    boolean containsSpecialCharacter,
    boolean containsUpperCase,
    boolean containsDigit,
    List<String> message,
    boolean isCommonPassword
){}
