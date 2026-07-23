package com.bizcontent.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Gemini generateContent 호출을 감싸는 얇은 클라이언트.
// 외부 SDK 없이 JDK 내장 HttpClient만 사용 (추가 의존성/비용 없음).
@Component
public class GeminiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String MODEL = "gemini-2.0-flash";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String generate(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY 환경변수가 설정되지 않았습니다.");
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + MODEL + ":generateContent?key=" + apiKey;

        String escapedPrompt = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");

        String body = """
                {
                  "contents": [{ "parts": [{ "text": "%s" }] }]
                }
                """.formatted(escapedPrompt);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 429) throw new RuntimeException("QUOTA_EXCEEDED");
            if (response.statusCode() == 402) throw new RuntimeException("PAYMENT_REQUIRED");
            if (response.statusCode() == 403) throw new RuntimeException("FORBIDDEN");
            if (response.statusCode() >= 400) {
                throw new RuntimeException("Gemini API 오류 (" + response.statusCode() + "): " + response.body());
            }

            return extractText(response.body());
        } catch (java.io.IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Gemini API 호출 실패", e);
        }
    }

    // 응답 JSON에서 "text" 필드 값만 뽑아내는 간단한 파서 (JSON 라이브러리 없이 정규식으로 최소 구현)
    private String extractText(String json) {
        Pattern pattern = Pattern.compile("\"text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        throw new RuntimeException("Gemini 응답 파싱 실패: " + json);
    }
}
