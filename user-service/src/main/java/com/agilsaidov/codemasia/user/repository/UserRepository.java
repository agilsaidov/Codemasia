package com.agilsaidov.codemasia.user.repository;

import com.agilsaidov.codemasia.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
