package com.agilsaidov.codemasia.user.group.repository;

import com.agilsaidov.codemasia.user.group.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, String>, JpaSpecificationExecutor<Group> {
    boolean existsByGroupId(String groupId);

    boolean existsByName(String name);

    @Query("""
    SELECT g FROM Group g
    JOIN FETCH g.createdBy
    WHERE g.groupId = :groupId
    """)
    Optional<Group> findByIdWithCreator(@Param("groupId") String groupId);
}
