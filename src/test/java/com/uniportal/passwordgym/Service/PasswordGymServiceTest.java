package com.uniportal.passwordgym.Service;

import com.uniportal.passwordgym.Dto.RequestDto;
import com.uniportal.passwordgym.Dto.ResponseDto;
import com.uniportal.passwordgym.Enum.StrengthScore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGymServiceTest {

    private final PasswordGymService passwordGymService = new PasswordGymService(10, 12, 16);

    private static final String USERNAME = "okenobi";
    private static final String EMAIL = "o.kenobi@jedi-council.com";

    private static RequestDto requestWithPassword(String password) {
        return new RequestDto(USERNAME, EMAIL, password);
    }

    @Test
    void tooShortPasswordShouldBeVeryWeak() {
        // given
        RequestDto request = requestWithPassword("abc123");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertFalse(response.meetsPolicy());
        assertEquals(StrengthScore.VERY_WEAK, response.strength());
    }

    @Test
    void passwordAtMinimumLengthShouldPassLengthCheck() {
        // given
        RequestDto request = requestWithPassword("Xk9qTmZv2r");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertTrue(response.meetsPolicy());
        assertNotEquals(StrengthScore.VERY_WEAK, response.strength());
    }

    @Test
    void commonPasswordShouldFailPolicy() {
        // given
        RequestDto request = requestWithPassword("qwertyuiop");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertFalse(response.meetsPolicy());
        assertTrue(response.isCommonPassword());
    }

    @Test
    void commonPasswordVariantWithSuffixShouldFailPolicy() {
        // given
        RequestDto request = requestWithPassword("Starwars1!");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertFalse(response.meetsPolicy());
        assertTrue(response.isCommonPassword());
    }

    @Test
    void passwordContainingUsernameShouldFailPolicy() {
        // given
        RequestDto request = requestWithPassword("okenobi2024");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertFalse(response.meetsPolicy());
        assertTrue(response.containsUsernameOrEmail());
    }

    @Test
    void passwordContainingEmailLocalPartShouldFailPolicy() {
        // given
        RequestDto request = requestWithPassword("myo.kenobipw");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertFalse(response.meetsPolicy());
        assertTrue(response.containsUsernameOrEmail());
    }

    @Test
    void threeOrMoreRepeatedCharsShouldFailPolicy() {
        // given
        RequestDto request = requestWithPassword("xxxAbcdef1");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertFalse(response.meetsPolicy());
    }

    @Test
    void twoRepeatedCharsShouldNotFailPolicy() {
        // given
        RequestDto request = requestWithPassword("MyHelloTag");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertTrue(response.meetsPolicy());
    }

    @Test
    void mixedSequentialThenRepeatShouldNotCountAsThreeRun() {
        // given:
        RequestDto request = requestWithPassword("Hulkbuster3100!");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertTrue(response.meetsPolicy());
    }

    @Test
    void passwordWithFullCompositionShouldGetBonusFlags() {
        // given
        RequestDto request = requestWithPassword("Tr7$mQ9!Zx");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertTrue(response.meetsPolicy());
        assertTrue(response.containsUpperCase());
        assertTrue(response.containsDigit());
        assertTrue(response.containsSpecialCharacter());
    }

    @Test
    void passwordWithNoCompositionShouldNotHaveBonusFlags() {
        // given
        RequestDto request = requestWithPassword("myfavoritebook");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertFalse(response.containsUpperCase());
        assertFalse(response.containsDigit());
        assertFalse(response.containsSpecialCharacter());
    }

    @Test
    void longPasswordWithNoCompositionShouldStillReachStrongOnLengthAlone() {
        // given
        RequestDto request = requestWithPassword("myfavoritebookshelf");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertTrue(response.meetsPolicy());
        assertEquals(StrengthScore.STRONG, response.strength());
    }

    @Test
    void passwordWithSpaceShouldBeTreatedAsSpecialCharacter() {
        // given
        RequestDto request = requestWithPassword("Hello there!");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertTrue(response.containsSpecialCharacter());
    }

    @Test
    void taskExamplePasswordShouldMeetPolicyAndBeStrong() {
        // given
        RequestDto request = requestWithPassword("Hello there!");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertTrue(response.meetsPolicy());
        assertEquals(StrengthScore.STRONG, response.strength());
    }

    @Test
    void missingCommonPasswordsFileShouldThrowIllegalStateException() {
        assertThrows(
                IllegalStateException.class,
                () -> PasswordGymService.loadCommonPasswords("/this-file-does-not-exist.txt")
        );
    }

    @Test
    void elevenCharacterPasswordWithNoBonusShouldBeWeak() {
        // given
        RequestDto request = requestWithPassword("mqzbwkfrycp");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertTrue(response.meetsPolicy());
        assertEquals(StrengthScore.WEAK, response.strength());
    }

    @Test
    void twelveCharacterPasswordWithNoBonusShouldBeGood() {
        // given
        RequestDto request = requestWithPassword("mqzbwkfrycpj");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertTrue(response.meetsPolicy());
        assertEquals(StrengthScore.GOOD, response.strength());
    }

    @Test
    void fifteenCharacterPasswordWithNoBonusShouldBeGood() {
        // given
        RequestDto request = requestWithPassword("mqzbwkfrycpjshx");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertTrue(response.meetsPolicy());
        assertEquals(StrengthScore.GOOD, response.strength());
    }

    @Test
    void sixteenCharacterPasswordWithNoBonusShouldBeStrong() {
        // given
        RequestDto request = requestWithPassword("mqzbwkfrycpjshxb");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertTrue(response.meetsPolicy());
        assertEquals(StrengthScore.STRONG, response.strength());
    }

    @Test
    void usernameMatchShouldBeCaseInsensitive() {
        // given
        RequestDto request = requestWithPassword("OKENOBI2024");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertFalse(response.meetsPolicy());
        assertTrue(response.containsUsernameOrEmail());
    }

    @Test
    void shortUsernameShouldNotCauseFalsePositiveIdentityMatch() {
        // given:
        RequestDto request = new RequestDto("jo", "jo@example.com", "ConjoinedPass1");

        // when
        ResponseDto response = passwordGymService.evaluate(request);

        // then
        assertFalse(response.containsUsernameOrEmail());
    }
}
