package com.bizcontent.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateRequestDto {

    @NotBlank(message = "주제를 입력해주세요.")
    private String topic; // 자유 입력 주제 (예: "지급명령 이의신청")

    private String targetKeyword; // 선택 - 비어있으면 topic에서 자동 추출

    private Long categoryId; // 사용자가 확정한 카테고리 (2단계에서 넘어옴)
}
