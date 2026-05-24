package com.agilsaidov.codemasia.user.group.repository;

import com.agilsaidov.codemasia.user.group.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
}
