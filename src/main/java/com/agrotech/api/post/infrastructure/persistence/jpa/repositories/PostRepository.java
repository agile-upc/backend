package com.agrotech.api.post.infrastructure.persistence.jpa.repositories;

import com.agrotech.api.post.infrastructure.persistence.jpa.entities.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findByAdvisor_Id(Long advisorId);
    List<PostEntity> findAllByOrderByUpdatedAtDesc();
}
