package com.bizcontent.app.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 업종/카테고리 - 법무사, 부동산, 병원, 맛집 등
// isRegulated = true 인 카테고리는 생성 시 규제 체크리스트가 자동 적용됨 (법무사/병원 등)
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // 예: "법률/법무사", "부동산", "병원", "맛집", "기타"

    @Column(nullable = false)
    private boolean regulated; // 규제 체크리스트 적용 대상 여부

    @Column(nullable = false)
    private boolean custom; // 사용자가 직접 입력해서 새로 생긴 카테고리인지

    // 이 카테고리 전용 프롬프트 규칙 (톤, 금지 표현, 강조 포인트 등)
    @Lob
    @Column(name = "prompt_rule", columnDefinition = "TEXT")
    private String promptRule;

    // 이 카테고리가 규제 대상일 때 체크할 항목들 (예: "치료효과 단정 표현 금지, 비교광고 금지")
    @Lob
    @Column(name = "compliance_checklist", columnDefinition = "TEXT")
    private String complianceChecklist;

    public Category(String name, boolean regulated, boolean custom, String promptRule, String complianceChecklist) {
        this.name = name;
        this.regulated = regulated;
        this.custom = custom;
        this.promptRule = promptRule;
        this.complianceChecklist = complianceChecklist;
    }
}
