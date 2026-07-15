package com._2.hungryworker.domain.user;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

/**
 * 신규 가입 시 "배고픈{동물}#{4자리 숫자}" 형식의 닉네임을 자동 생성한다.
 * 예: 배고픈다람쥐#1234
 * 동일 닉네임이 이미 존재하면 숫자를 다시 뽑아 중복을 피한다.
 */
@Component
public class NicknameGenerator {

    private static final String PREFIX = "배고픈";

    private static final List<String> ANIMALS = List.of(
        "코알라", "캥거루", "쿼카", "다람쥐", "토끼", "웜뱃", "사슴"
    );

    private final UserRepository userRepository;

    public NicknameGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generate() {
        String nickname;
        do {
            nickname = PREFIX + randomAnimal() + "#" + randomFourDigits();
        } while (userRepository.existsByNickname(nickname));

        return nickname;
    }

    private String randomAnimal() {
        int index = ThreadLocalRandom.current().nextInt(ANIMALS.size());
        return ANIMALS.get(index);
    }

    private String randomFourDigits() {
        int number = ThreadLocalRandom.current().nextInt(10000); // 0 ~ 9999
        return String.format("%04d", number);
    }
}
