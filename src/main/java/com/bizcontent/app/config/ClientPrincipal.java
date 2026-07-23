package com.bizcontent.app.config;

import com.bizcontent.app.domain.Client;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// Spring Security가 로그인 세션에 들고 다니는 principal 객체.
// 컨트롤러에서 @AuthenticationPrincipal ClientPrincipal principal 로 바로 꺼내 쓸 수 있음.
@Getter
public class ClientPrincipal implements UserDetails {

    private final Client client;

    public ClientPrincipal(Client client) {
        this.client = client;
    }

    public Long getClientId() {
        return client.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_CLIENT"));
    }

    @Override
    public String getPassword() {
        return client.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return client.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}
