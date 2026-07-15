package com._2.hungryworker.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameRequest(
    @NotBlank
    @Size(max = 50)
    String nickname
) {
}
