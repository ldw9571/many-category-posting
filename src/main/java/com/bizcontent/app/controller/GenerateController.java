package com.bizcontent.app.controller;

import com.bizcontent.app.config.ClientPrincipal;
import com.bizcontent.app.domain.Category;
import com.bizcontent.app.domain.Client;
import com.bizcontent.app.domain.Post;
import com.bizcontent.app.dto.CategorySuggestionDto;
import com.bizcontent.app.dto.GenerateRequestDto;
import com.bizcontent.app.repository.CategoryRepository;
import com.bizcontent.app.repository.ClientRepository;
import com.bizcontent.app.repository.PostRepository;
import com.bizcontent.app.service.CategoryClassifierService;
import com.bizcontent.app.service.ContentGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/generate")
@RequiredArgsConstructor
public class GenerateController {

    private final CategoryClassifierService categoryClassifierService;
    private final ContentGeneratorService contentGeneratorService;
    private final CategoryRepository categoryRepository;
    private final ClientRepository clientRepository;
    private final PostRepository postRepository;

    // 화면 1: 주제 입력
    @GetMapping
    public String showInputForm(Model model) {
        model.addAttribute("generateRequest", new GenerateRequestDto());
        return "generate";
    }

    // 화면 1 -> 화면 2: 주제 제출 시 AI가 카테고리 추천, 확인 화면 표시
    @PostMapping("/suggest")
    public String suggestCategory(@ModelAttribute GenerateRequestDto dto, Model model) {
        CategorySuggestionDto suggestion = categoryClassifierService.suggest(dto.getTopic());

        model.addAttribute("topic", dto.getTopic());
        model.addAttribute("targetKeyword", dto.getTargetKeyword());
        model.addAttribute("suggestion", suggestion);
        model.addAttribute("categories", categoryRepository.findAll());
        return "category-confirm";
    }

    // 화면 2 -> 화면 3: 사용자가 카테고리 확정 -> 실제 생성 + 이력 저장
    @PostMapping("/confirm")
    public String confirmAndGenerate(@ModelAttribute GenerateRequestDto dto,
                                      @AuthenticationPrincipal ClientPrincipal principal,
                                      Model model) {
        Client client = clientRepository.findById(principal.getClientId())
                .orElseThrow(() -> new IllegalStateException("고객 정보를 찾을 수 없습니다."));

        if (!client.canUseThisMonth()) {
            model.addAttribute("errorMsg", "이번 달 사용 가능 건수(" + client.getMonthlyLimit() + "건)를 모두 사용했습니다.");
            return "generate";
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));

        ContentGeneratorService.GenerationResult result =
                contentGeneratorService.generate(dto.getTopic(), dto.getTargetKeyword(), category);

        Post post = new Post();
        post.setClient(client);
        post.setTopic(dto.getTopic());
        post.setCategory(category);
        post.setContent(result.content());
        post.setTitleLength(result.titleLength());
        post.setKeywordDensity(result.keywordDensity());
        post.setComplianceRisk(result.complianceRisk());
        postRepository.save(post);

        client.increaseUsage();
        clientRepository.save(client);

        model.addAttribute("post", post);
        return "result";
    }
}
