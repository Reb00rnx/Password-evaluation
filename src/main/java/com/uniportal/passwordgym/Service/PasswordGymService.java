package com.uniportal.passwordgym.Service;

import com.uniportal.passwordgym.Dto.RequestDto;
import com.uniportal.passwordgym.Dto.ResponseDto;
import com.uniportal.passwordgym.Enum.StrengthScore;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PasswordGymService {

    private final Set<String> commonPasswords = loadCommonPasswords();

    public ResponseDto evaluate(RequestDto requestDto) {
        String password = requestDto.password();
        String username = requestDto.username();
        String email = requestDto.email();

        int score = scoreLength(password);
        List<String> messages = new ArrayList<>();
        boolean meetsPolicy = true;
        boolean containsUsernameOrEmail = containsUsernameOrEmail(password, username, email);
        boolean commonPassword = matchesCommonPasswordList(password);
        boolean upperCase = containsUpperCase(password);
        boolean digit = containsDigit(password);
        boolean specialCharacter = containsSpecialChar(password);


        if (password.length() < 10) {
            meetsPolicy = false;
            messages.add("Password must be at least 10 characters long");
        }
        if (upperCase) {
            score += 1;
        } else {
            messages.add("Consider adding an upper case letter for a stronger password");
        }
        if (digit) {
            score += 1;
        } else {
            messages.add("Consider adding a digit for a stronger password");
        }
        if (specialCharacter) {
            score += 1;
        } else {
            messages.add("Consider adding a special character for a stronger password");
        }
        if (commonPassword) {
            meetsPolicy = false;
            messages.add("Password is on the list of commonly used passwords");
        }
        if (containsUsernameOrEmail) {
            meetsPolicy = false;
            messages.add("Password contains the username or email address");
        }
        if (hasSequentialOrRepeatedChars(password)) {
            meetsPolicy = false;
            messages.add("Password contains >2 repeated or sequential characters");
        }


        if (meetsPolicy){
            score = Math.max(score, 0);
        }
        else {
            score = 0;
        }

        StrengthScore strength;

        if (password.length() < 10) {
            strength = StrengthScore.VERY_WEAK;
        } else if (!meetsPolicy) {
            strength = StrengthScore.WEAK;
        } else {
            strength = mapToStrength(score);
        }

        return new ResponseDto(score, strength, meetsPolicy, containsUsernameOrEmail, specialCharacter, upperCase, digit, messages, commonPassword);

    }

    private boolean containsUpperCase(String password) {
    for (char c : password.toCharArray()) {
        if (Character.isUpperCase(c)) return true;
    }
    return false;
    }

    private boolean containsDigit(String password) {
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) return true;
        }
        return false;
    }

    private boolean containsSpecialChar(String password) {
        for (char c : password.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) return true;
        }
        return false;
    }



    private StrengthScore mapToStrength(int score) {
        if (score <= 0) return StrengthScore.VERY_WEAK;
        if (score <= 5) return StrengthScore.WEAK;
        if (score <= 7) return StrengthScore.FAIR;
        if (score <= 9) return StrengthScore.GOOD;
        return StrengthScore.STRONG;
    }

    private int scoreLength(String password) {
        int score = 0;

        if (password.length() >= 10) {
            score = 5;
        }
        if (password.length() >= 12) {
            score = 8;
        }
        if (password.length() >= 16) {
            score = 10;
        }

        return score;
    }

    private boolean matchesCommonPasswordList(String password) {
    String lower = password.toLowerCase();
    if (commonPasswords.contains(lower)) {
        return true;
    }
    String stripped = lower.replaceAll("[^a-z]+$", "");
    return commonPasswords.contains(stripped);
    }

    private boolean containsUsernameOrEmail(String password, String username, String email) {
        boolean containsUsername = password.toLowerCase().contains(username.toLowerCase());
        boolean containsEmail = password.toLowerCase().contains(email.split("@")[0].toLowerCase());

        return containsEmail || containsUsername;
    }

    private boolean hasSequentialOrRepeatedChars(String password) {
        int runLength = 1;

        for (int i = 1; i < password.length(); i++) {
            char prev = password.charAt(i - 1);
            char curr = password.charAt(i);

            boolean isRepeat = curr == prev;
            boolean isSequential = curr == prev + 1 || curr == prev - 1;

            if (isRepeat || isSequential) {
                runLength++;
                if (runLength >= 3) {
                    return true;
                }
            } else {
                runLength = 1;
            }
        }

        return false;
    }

    private static Set<String> loadCommonPasswords() {
        InputStream is = PasswordGymService.class.getResourceAsStream("/top_most_used.txt");
        if (is == null) {
            throw new IllegalStateException("Common passwords file not found on classpath: /top_most_used.txt");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
