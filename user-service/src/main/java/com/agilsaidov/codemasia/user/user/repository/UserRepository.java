package com.agilsaidov.codemasia.user.user.repository;

import com.agilsaidov.codemasia.user.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> getUserByUserId(UUID userId);

    boolean existsByEmailAndUserIdNot(String email, UUID userId);

    boolean existsByEmailOrUsername(String email, String username);
}
