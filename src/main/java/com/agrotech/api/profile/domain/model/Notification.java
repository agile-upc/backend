package com.agrotech.api.profile.domain.model;

import com.agrotech.api.iam.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String message;

    @Column(name = "send_at", nullable = false)
    private Date sendAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
