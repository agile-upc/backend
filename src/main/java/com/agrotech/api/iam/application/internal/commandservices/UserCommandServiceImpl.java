package com.agrotech.api.iam.application.internal.commandservices;

import com.agrotech.api.iam.application.internal.outboundservices.acl.ExternalProfileRoleService;
import com.agrotech.api.iam.application.internal.outboundservices.hashing.HashingService;
import com.agrotech.api.iam.application.internal.outboundservices.tokens.TokenService;
import com.agrotech.api.iam.domain.exceptions.InvalidPasswordException;
import com.agrotech.api.iam.domain.exceptions.InvalidRoleException;
import com.agrotech.api.iam.domain.exceptions.UserNotFoundInSignInException;
import com.agrotech.api.iam.domain.exceptions.UsernameAlreadyExistsException;
import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.iam.domain.model.commands.SignInCommand;
import com.agrotech.api.iam.domain.model.commands.SignUpCommand;
import com.agrotech.api.iam.domain.services.UserCommandService;
import com.agrotech.api.iam.infrastructure.persistence.jpa.entities.RoleEntity;
import com.agrotech.api.iam.infrastructure.persistence.jpa.entities.UserEntity;
import com.agrotech.api.iam.infrastructure.persistence.jpa.mappers.RoleMapper;
import com.agrotech.api.iam.infrastructure.persistence.jpa.mappers.UserMapper;
import com.agrotech.api.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.agrotech.api.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleRepository roleRepository;
    private final ExternalProfileRoleService externalProfileRoleService;

    public UserCommandServiceImpl(UserRepository userRepository, HashingService hashingService, TokenService tokenService, RoleRepository roleRepository, ExternalProfileRoleService externalProfileRoleService) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
        this.externalProfileRoleService = externalProfileRoleService;
    }

    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        var user = userRepository.findByUsername(command.username())
                .orElseThrow(() -> new UserNotFoundInSignInException(command.username()));
        if (!hashingService.matches(command.password(), user.getPassword()))
            throw new InvalidPasswordException();

        var roles = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .toList();

        var token = tokenService.generateToken(user.getUsername(), roles);
        return Optional.of(ImmutablePair.of(UserMapper.toDomain(user), token));
    }

    @Override
    @Transactional
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username())) throw new UsernameAlreadyExistsException();
        var roles = command.roles().stream().map(role -> roleRepository.findByName(role.getName()).orElseThrow(() -> new InvalidRoleException(role.getStringName()))).toList();
        var userEntity =
                UserEntity.builder()
                .username(command.username())
                .password(hashingService.encode(command.password()))
                .roles(new java.util.HashSet<>(roles))
                .build();
        userRepository.save(userEntity);
        //Create a farmer or advisor depending on the role
        roles.stream().map(RoleMapper::toDomain).forEach(role -> {
            if (role.getStringName().equals("ROLE_FARMER")) {
                externalProfileRoleService.createFarmer(userEntity.getId(), UserMapper.toDomain(userEntity));
            }
            if (role.getStringName().equals("ROLE_ADVISOR")) {
                externalProfileRoleService.createAdvisor(userEntity.getId(), UserMapper.toDomain(userEntity));
            }
        });
        return userRepository.findByUsername(command.username()).map(UserMapper::toDomain);
    }
}