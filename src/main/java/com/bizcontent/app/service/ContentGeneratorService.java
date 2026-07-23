package com.bizcontent.app.service;

import com.bizcontent.app.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentGeneratorService {

    private final GeminiClient geminiClient;

    public record GenerationResult(String content, String title, int titleLength,
                                    double keywordDensity, boolean complianceRisk) {}

    public GenerationResult generate(String topic, String targetKeyword, Category category) {
        String keyword = (targetKeyword == null || targetKeyword.isBlank()) ? topic : targetKeyword;

        // 카테고리별 프롬프트 규칙 + (규제 대상이면) 체크리스트를 함께 반영
        String prompt = """
                아래 조건에 맞는 네이버 블로그 글을 작성해줘.

                주제: %s
                타겟 키워드: %s (본문에 자연스럽게 반복 포함)
                업종: %s
                작성 규칙: %s
                %s

                출력 형식은 반드시 아래와 같이:
                제목: (블로그 제목)
                본문: (본문 내용)
                """.formatted(
                        topic,
                        keyword,
                        category.getName(),
                        category.getPromptRule(),
                        category.isRegulated()
                                ? "규제 주의사항 (반드시 지킬 것): " + category.getComplianceChecklist()
                                : ""
                );

        String raw = geminiClient.generate(prompt);
        String title = extractTitle(raw);
        String content = raw;

        double keywordDensity = calcKeywordDensity(content, keyword);
        boolean complianceRisk = category.isRegulated() && containsRiskyPhrase(content);

        return new GenerationResult(content, title, title.length(), keywordDensity, complianceRisk);
    }

    private String extractTitle(String raw) {
        for (String line : raw.split("\n")) {
            if (line.trim().startsWith("제목")) {
                return line.replaceFirst("^제목\\s*:\\s*", "").trim();
            }
        }
        // 제목 줄을 못 찾으면 첫 줄을 제목으로 대체
        return raw.lines().findFirst().orElse("").trim();
    }

    private double calcKeywordDensity(String content, String keyword) {
        if (content.isBlank() || keyword.isBlank()) return 0.0;
        int totalChars = content.replaceAll("\\s", "").length();
        int keywordChars = keyword.replaceAll("\\s", "").length();
        int occurrences = countOccurrences(content, keyword);
        if (totalChars == 0) return 0.0;
        return Math.round((occurrences * keywordChars * 100.0 / totalChars) * 10) / 10.0;
    }

    private int countOccurrences(String text, String sub) {
        int count = 0, idx = 0;
        while ((idx = text.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    // 1차 버전: 체크리스트에 등록된 위험 표현이 본문에 그대로 포함되어 있는지만 간단히 스캔.
    // 실제 의료법/변호사법 문구는 더 정교한 규칙이 필요해서, 이건 최소 안전장치로만 쓰고
    // 최종 발행 전에는 반드시 사람이 검토하도록 안내해야 함 (그래서 기본 publishMode가 DRAFT_ONLY).
    private boolean containsRiskyPhrase(String content) {
        String[] riskyPhrases = {"100% 완치", "반드시 효과", "부작용 없음", "무조건 승소"};
        for (String phrase : riskyPhrases) {
            if (content.contains(phrase)) return true;
        }
        return false;
    }
}
