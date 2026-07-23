package com.bizcontent.app.controller;

import com.bizcontent.app.config.ClientPrincipal;
import com.bizcontent.app.domain.Client;
import com.bizcontent.app.repository.ClientRepository;
import com.bizcontent.app.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ClientRepository clientRepository;
    private final PostRepository postRepository;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal ClientPrincipal principal, Model model) {
        Client client = clientRepository.findById(principal.getClientId())
                .orElseThrow(() -> new IllegalStateException("고객 정보를 찾을 수 없습니다."));

        model.addAttribute("client", client);
        model.addAttribute("posts", postRepository.findByClientOrderByCreatedAtDesc(client));
        return "dashboard";
    }
}
