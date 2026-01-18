package com.thinhpay.backend.modules.iam.infrastructure.persistence.adapter;

import com.thinhpay.backend.modules.iam.application.port.out.UserRepository;
import com.thinhpay.backend.modules.iam.domain.user.IamUser;
import com.thinhpay.backend.modules.iam.infrastructure.persistence.jpa.IamUserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;


@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserRepositoryAdapter implements UserRepository {

    IamUserRepository jpaRepository;

    @Override
    public Optional<IamUser> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<IamUser> findByUsername(String username) {
        // IamUser không có username field riêng, dùng email hoặc phone làm identifier
        return jpaRepository.findByEmailOrPhoneNumber(username);
    }

    @Override
    public Optional<IamUser> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public Optional<IamUser> findByUsernameOrEmail(String identifier) {
        return jpaRepository.findByEmailOrPhoneNumber(identifier);
    }

    @Override
    public IamUser save(IamUser user) {
        return jpaRepository.save(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        // Check exists bằng cách dùng findByEmailOrPhoneNumber
        return jpaRepository.findByEmailOrPhoneNumber(username).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}

