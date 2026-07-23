package com.bizcontent.app.service;

import com.bizcontent.app.domain.Category;
import com.bizcontent.app.dto.CategorySuggestionDto;
import com.bizcontent.app.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// 자유 입력 주제(예: "지급명령 이의신청")를 보고, 기존 카테고리 중 가장 맞는 것을 AI가 추천.
// 최종 확정은 사용자가 화면에서 직접 함 (AI 추천은 참고용, 강제 아님).
@Service
@RequiredArgsConstructor
public class CategoryClassifierService {

    private final GeminiClient geminiClient;
    private final CategoryRepository categoryRepository;

    private static final Pattern RESULT_PATTERN =
            Pattern.compile("카테고리\\s*:\\s*(.+?)\\s*[,\\n]\\s*확신도\\s*:\\s*(\\d+)");

    public CategorySuggestionDto suggest(String topic) {
        List<Category> categories = categoryRepository.findAll();
        String categoryNames = categories.stream()
                .map(Category::getName)
                .collect(Collectors.joining(", "));

        String prompt = """
                다음 카테고리 목록 중 아래 주제와 가장 잘 맞는 카테고리 하나를 골라줘.
                카테고리 목록: %s

                주제: "%s"

                반드시 아래 형식으로만 답해. 다른 설명은 절대 붙이지 마.
                카테고리: (카테고리 이름), 확신도: (0~100 사이 숫자)
                """.formatted(categoryNames, topic);

        String response = geminiClient.generate(prompt);
        Matcher matcher = RESULT_PATTERN.matcher(response);

        if (matcher.find()) {
            String name = matcher.group(1).trim();
            int confidence = Integer.parseInt(matcher.group(2).trim());
            return new CategorySuggestionDto(name, confidence);
        }

        // 파싱 실패 시 "기타"로 안전하게 폴백
        return new CategorySuggestionDto("기타", 0);
    }
}
