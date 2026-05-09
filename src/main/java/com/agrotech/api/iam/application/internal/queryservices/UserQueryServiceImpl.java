package com.agrotech.api.iam.application.internal.queryservices;

import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.iam.domain.model.queries.GetAllUsersQuery;
import com.agrotech.api.iam.domain.model.queries.GetUserByIdQuery;
import com.agrotech.api.iam.domain.model.queries.GetUserByUsernameQuery;
import com.agrotech.api.iam.domain.services.UserQueryService;
import com.agrotech.api.iam.infrastructure.persistence.jpa.mappers.UserMapper;
import com.agrotech.api.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> handle(GetAllUsersQuery query) {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.userId())
                .map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> handle(GetUserByUsernameQuery query) {
        return userRepository.findByUsername(query.username())
                .map(UserMapper::toDomain);
    }
}