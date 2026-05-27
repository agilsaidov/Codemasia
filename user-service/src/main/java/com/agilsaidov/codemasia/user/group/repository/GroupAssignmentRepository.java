package com.agilsaidov.codemasia.user.group.repository;

import com.agilsaidov.codemasia.user.group.model.GroupAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupAssignmentRepository extends JpaRepository<GroupAssignment, Long> {
    @Query("""
    SELECT ga FROM GroupAssignment ga
    JOIN FETCH ga.teacher
    WHERE ga.group.groupId = :groupId
    ORDER BY ga.assignedAt DESC
    """)
    List<GroupAssignment> findAllWithTeacherByGroupId(@Param("groupId") String groupId);

    Optional<GroupAssignment> findByGroup_GroupIdAndTeacher_UserId(String groupId, Long userId);
}
