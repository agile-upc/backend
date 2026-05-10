package com.agrotech.api.post.infrastructure.persistence.jpa.repository;

import com.agrotech.api.post.domain.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAdvisor_Id(Long advisorId);
    List<Post> findAllByOrderByUpdatedAtDesc();
}
