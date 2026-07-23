package com.bizcontent.app.repository;

import com.bizcontent.app.domain.Client;
import com.bizcontent.app.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByClientOrderByCreatedAtDesc(Client client);
}
