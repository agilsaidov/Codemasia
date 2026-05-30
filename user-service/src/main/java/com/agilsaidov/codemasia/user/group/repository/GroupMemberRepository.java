package com.agilsaidov.codemasia.user.group.repository;

import com.agilsaidov.codemasia.user.group.model.GroupMember;
import com.agilsaidov.codemasia.user.group.model.GroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberId> {

    List<GroupMember> findByGroup_GroupIdAndId_UserIdIn(String groupId, Collection<UUID> userIds);

    @Query("""
            SELECT gm.id.groupId, COUNT(gm)
            FROM GroupMember gm
            WHERE gm.id.groupId IN :groupIds
              AND gm.enabled = true
            GROUP BY gm.id.groupId
            """)
    List<Object[]> countMembersByGroupIds(@Param("groupIds") Collection<String> groupIds);

    @Query("""
    SELECT gm FROM GroupMember gm
    JOIN FETCH gm.user
    WHERE gm.group.groupId = :groupId
    """)
    List<GroupMember> findAllWithUserByGroupId(@Param("groupId") String groupId);}
