package com.uniportal.passwordgym.Service;

import com.uniportal.passwordgym.Dto.RequestDto;
import com.uniportal.passwordgym.Dto.ResponseDto;
import com.uniportal.passwordgym.Enum.StrengthScore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PasswordGymService {

    private static final int MIN_IDENTITY_MATCH_LENGTH = 3;

    private final Set<String> commonPasswords;
    private final int minLength;
    private final int midLength;
    private final int longLength;

    public PasswordGymService(
            @Value("${password.policy.min-length}") int minLength,
            @Value("${password.policy.mid-length}") int midLength,
            @Value("${password.policy.long-length}") int longLength) {
        this.minLength = minLength;
        this.midLength = midLength;
        this.longLength = longLength;
        this.commonPasswords = loadCommonPasswords();
    }

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


        if (password.length() < minLength) {
            meetsPolicy = false;
            messages.add("Password must be at least " + minLength + " characters long");
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

        if (password.length() < minLength) {
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

        if (password.length() >= minLength) {
            score = 5;
        }
        if (password.length() >= midLength) {
            score = 8;
        }
        if (password.length() >= longLength) {
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
        String emailLocalPart = email.split("@")[0];
        String lowerPassword = password.toLowerCase();

        boolean containsUsername = username.length() >= MIN_IDENTITY_MATCH_LENGTH
                && lowerPassword.contains(username.toLowerCase());
        boolean containsEmail = emailLocalPart.length() >= MIN_IDENTITY_MATCH_LENGTH
                && lowerPassword.contains(emailLocalPart.toLowerCase());

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
        return loadCommonPasswords("/top_most_used.txt");
    }

    static Set<String> loadCommonPasswords(String resourcePath) {
        InputStream is = PasswordGymService.class.getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IllegalStateException("Common passwords file not found on classpath: " + resourcePath);
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
