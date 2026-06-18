package com.agilsaidov.codemasia.exam.repository;

import com.agilsaidov.codemasia.exam.model.ExamSession;
import com.agilsaidov.codemasia.exam.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {

    List<ExamSession> findAllByExam_ExamIdAndStatus(String examId, SessionStatus status);

    boolean existsByExam_ExamIdAndStatus(String examId, SessionStatus status);

    long countByExam_ExamId(String examId);

    @Query("""
        SELECT COUNT(e) > 0
        FROM ExamSession e
        WHERE e.groupId = :groupId
          AND e.enabled = true
          AND e.status IN :activeStatuses
          AND :startsAt < e.endsAt AND :endsAt > e.startsAt
    """)
    boolean existsOverlappingSession(@Param("groupId") String groupId,
                                     @Param("startsAt") OffsetDateTime startsAt,
                                     @Param("endsAt") OffsetDateTime endsAt,
                                     @Param("activeStatuses") List<SessionStatus> activeStatuses);
}
