package com.agilsaidov.codemasia.exam.repository;

import com.agilsaidov.codemasia.exam.model.ExamSessionLanguage;
import com.agilsaidov.codemasia.exam.model.ExamSessionLanguageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamSessionLanguageRepository extends JpaRepository<ExamSessionLanguage, ExamSessionLanguageId> {

    @Query("""
            SELECT esl FROM ExamSessionLanguage esl
            JOIN FETCH esl.language
            WHERE esl.session.examSessionId = :examSessionId
        """)
    List<ExamSessionLanguage> findAllBySessionIdWithLanguages(@Param("examSessionId") Long examSessionId);

    int countBySession_ExamSessionId(Long sessionId);
}
