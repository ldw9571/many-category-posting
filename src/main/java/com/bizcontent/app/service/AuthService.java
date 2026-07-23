package com.bizcontent.app.service;

import com.bizcontent.app.domain.Client;
import com.bizcontent.app.dto.RegisterDto;
import com.bizcontent.app.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterDto dto) {
        if (clientRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        Client client = new Client(
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()), // 평문 비밀번호는 여기서 즉시 해시로 변환, 저장 안 함
                dto.getBusinessName()
        );
        clientRepository.save(client);
    }
}
