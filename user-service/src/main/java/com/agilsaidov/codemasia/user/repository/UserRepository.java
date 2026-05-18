package com.agilsaidov.codemasia.user.repository;

import com.agilsaidov.codemasia.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> getUserByKeycloakId(UUID keycloakId);

    Optional<User> getUserByUserId(Long userId);
}
