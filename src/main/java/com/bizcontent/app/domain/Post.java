package com.bizcontent.app.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// 생성된 글 1건 = 발행 이력 1행
@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private String topic; // 사용자가 입력한 원문 주제

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // 사용자가 최종 확정한 카테고리

    // AI가 최초 추천했던 카테고리명 - 실제 확정값과 비교해서 추천 정확도 개선에 사용
    private String aiSuggestedCategory;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    private boolean complianceRisk = false; // 규제 위험 문구 감지 여부

    private Integer titleLength;
    private Double keywordDensity;

    @Enumerated(EnumType.STRING)
    private PostStatus status = PostStatus.GENERATED;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum PostStatus {
        GENERATED,   // 생성 완료, 아직 발행 안 함
        DRAFT_SAVED, // 네이버 임시저장 완료
        PUBLISHED    // 실제 발행 완료
    }
}
