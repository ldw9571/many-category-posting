package com.bizcontent.app.domain;

import com.bizcontent.app.config.TokenEncryptConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// 고객사 계정 (로그인 주체)
@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email; // 로그인 아이디로 사용

    @Column(nullable = false)
    private String passwordHash; // BCrypt 해시만 저장, 평문 절대 저장 안 함

    @Column(nullable = false)
    private String businessName; // 상호명 (예: "OO법무사", "OO병원")

    @Column(nullable = false)
    private Integer monthlyLimit = 30; // 월 사용 가능 건수 (구독 플랜에 따라)

    @Column(nullable = false)
    private Integer usedThisMonth = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PublishMode publishMode = PublishMode.DRAFT_ONLY; // 기본값: 안전하게 임시저장

    // 네이버 OAuth 접근 토큰 - DB 컬럼에는 암호화된 값만 저장됨
    // TokenEncryptConverter가 저장/조회 시 자동으로 암복호화 처리
    @Convert(converter = TokenEncryptConverter.class)
    @Column(name = "naver_access_token")
    private String naverAccessToken;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Client(String email, String passwordHash, String businessName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.businessName = businessName;
    }

    public boolean canUseThisMonth() {
        return usedThisMonth < monthlyLimit;
    }

    public void increaseUsage() {
        this.usedThisMonth++;
    }
}
