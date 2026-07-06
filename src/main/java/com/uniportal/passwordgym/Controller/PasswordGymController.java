package com.uniportal.passwordgym.Controller;

import com.uniportal.passwordgym.Dto.RequestDto;
import com.uniportal.passwordgym.Dto.ResponseDto;
import com.uniportal.passwordgym.Service.PasswordGymService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/password")
@CrossOrigin(origins = {"http://localhost:5173", "https://passwordgym.netlify.app"})
public class PasswordGymController {

    private final PasswordGymService passwordGymService;

    public PasswordGymController(PasswordGymService passwordGymService) {
        this.passwordGymService = passwordGymService;
    }


    @PostMapping("/evaluate")
    public ResponseEntity<ResponseDto> evaluate(@Valid @RequestBody RequestDto requestDto){
        return  ResponseEntity.ok(passwordGymService.evaluate(requestDto));
    }
}
