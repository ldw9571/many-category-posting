package com.bizcontent.app.config;

import com.bizcontent.app.domain.Category;
import com.bizcontent.app.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategorySeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) return;

        categoryRepository.save(new Category(
                "법률/법무사", true, false,
                "신뢰감 있는 톤, 정확한 법률 용어 사용, 과장 없이 절차 중심으로 설명",
                "변호사법 위반 소지 문구 금지, 승소 보장 표현 금지, 특정 결과 단정 금지"
        ));
        categoryRepository.save(new Category(
                "병원", true, false,
                "전문성과 신뢰를 강조하되 과장 금지, 환자 안심시키는 톤",
                "치료효과 단정 표현 금지, 비교광고 금지, 부작용 없음 등 단정적 표현 금지"
        ));
        categoryRepository.save(new Category(
                "부동산", false, false,
                "입지/시세 정보 중심, 신뢰감 있는 톤",
                null
        ));
        categoryRepository.save(new Category(
                "맛집", false, false,
                "구어체, 방문 후기 느낌, 먹음직스러운 묘사, 노골적 광고 문구 자제",
                null
        ));
        categoryRepository.save(new Category(
                "자영업/소상공인", false, false,
                "친근한 구어체, 지역 밀착형 톤",
                null
        ));
        categoryRepository.save(new Category(
                "기타", false, true,
                "중립적이고 일반적인 정보성 톤",
                null
        ));
    }
}
