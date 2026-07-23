package com.bizcontent.app.domain;

// 완전자동발행 vs 임시저장(고객 최종 검토 후 직접 발행)
public enum PublishMode {
    AUTO_PUBLISH,
    DRAFT_ONLY
}
