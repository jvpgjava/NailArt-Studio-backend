package com.nailart.application.users;

import com.nailart.infrastructure.persistence.entity.UserEntity;
import com.nailart.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserJpaRepository userRepo;

    @Transactional(readOnly = true)
    public Optional<UserEntity> findByKeycloakId(String keycloakId) {
        return userRepo.findByKeycloakId(keycloakId);
    }

    @Transactional(readOnly = true)
    public UserEntity getById(UUID id) {
        return userRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<UserEntity> listAll() {
        return userRepo.findAll();
    }

    @Transactional
    public UserEntity createOrUpdateFromKeycloak(String keycloakId, String email, String fullName, String phone) {
        return userRepo.findByKeycloakId(keycloakId)
                .map(u -> {
                    u.setEmail(email);
                    u.setFullName(fullName);
                    u.setPhone(phone);
                    return userRepo.save(u);
                })
                .orElseGet(() -> {
                    UserEntity u = UserEntity.builder()
                            .keycloakId(keycloakId)
                            .email(email)
                            .fullName(fullName)
                            .phone(phone != null ? phone : "")
                            .blocked(false)
                            .build();
                    return userRepo.save(u);
                });
    }

    @Transactional
    public UserEntity updateProfile(UUID id, String fullName, String phone) {
        UserEntity u = getById(id);
        if (fullName != null) u.setFullName(fullName);
        if (phone != null) u.setPhone(phone);
        return userRepo.save(u);
    }

    @Transactional
    public UserEntity setBlocked(UUID id, boolean blocked) {
        UserEntity u = getById(id);
        u.setBlocked(blocked);
        return userRepo.save(u);
    }
}
