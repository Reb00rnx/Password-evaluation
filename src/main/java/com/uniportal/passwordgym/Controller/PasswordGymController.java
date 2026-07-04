package com.uniportal.passwordgym.Controller;

import com.uniportal.passwordgym.Dto.RequestDto;
import com.uniportal.passwordgym.Dto.ResponseDto;
import com.uniportal.passwordgym.Service.PasswordGymService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/password")
public class PasswordGymController {

    private PasswordGymService passwordGymService;

    public PasswordGymController(PasswordGymService passwordGymService) {
        this.passwordGymService = passwordGymService;
    }


    @PostMapping("/validation")
    public ResponseEntity<ResponseDto> validate(@Valid @RequestBody RequestDto requestDto){
        return  ResponseEntity.ok(passwordGymService.validatePassword(requestDto));
    }
}
