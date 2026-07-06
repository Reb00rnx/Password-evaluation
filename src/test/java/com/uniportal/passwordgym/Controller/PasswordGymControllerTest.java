package com.uniportal.passwordgym.Controller;

import com.uniportal.passwordgym.Dto.ResponseDto;
import com.uniportal.passwordgym.Enum.StrengthScore;
import com.uniportal.passwordgym.Service.PasswordGymService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PasswordGymController.class)
class PasswordGymControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PasswordGymService passwordGymService;

    @Test
    void validRequestShouldReturnOk() throws Exception {
        ResponseDto fakeResponse = new ResponseDto(10,
                StrengthScore.STRONG,
                true,
                false,
                true,
                true,
                false, List.of(),
                false);
        when(passwordGymService.evaluate(any())).thenReturn(fakeResponse);

        mockMvc.perform(post("/api/v1/password/evaluate").contentType(MediaType.APPLICATION_JSON).content("""
                        {"username":"okenobi","email":"o.kenobi@jedi-council.com","password":"Hello there!"}
                        """)).andExpect(status().isOk());
    }

    @Test
    void blankUsernameShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/password/evaluate").contentType(MediaType.APPLICATION_JSON).content("""
                        {"username":"","email":"o.kenobi@jedi-council.com","password":"Hello there!"}
                        """)).andExpect(status().isBadRequest());
    }

    @Test
    void invalidEmailFormatShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/password/evaluate").contentType(MediaType.APPLICATION_JSON).content("""
                        {"username":"okenobi","email":"not-an-email","password":"Hello there!"}
                        """)).andExpect(status().isBadRequest());
    }

    @Test
    void passwordOverMaxLengthShouldReturnBadRequest() throws Exception {
        String tooLongPassword = "a".repeat(129);

        mockMvc.perform(post("/api/v1/password/evaluate").contentType(MediaType.APPLICATION_JSON).content("""
                        {"username":"okenobi","email":"o.kenobi@jedi-council.com","password":"%s"}
                        """.formatted(tooLongPassword)))
                .andExpect(status().isBadRequest());
    }
}