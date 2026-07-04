package com.uniportal.passwordgym.Service;

import com.uniportal.passwordgym.Dto.RequestDto;
import com.uniportal.passwordgym.Dto.ResponseDto;
import com.uniportal.passwordgym.Scoring.PasswordScoring;
import org.springframework.stereotype.Service;

@Service
public class PasswordGymService {

    private final PasswordScoring passwordScoring;

    public PasswordGymService(PasswordScoring passwordScoring) {
        this.passwordScoring = passwordScoring;
    }

    public ResponseDto validatePassword(RequestDto requestDto) {
     return null;
    }
}
