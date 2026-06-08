package com.agilsaidov.codemasia.exam.repository;

import com.agilsaidov.codemasia.exam.model.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExamRepository extends JpaRepository<Exam, String>, JpaSpecificationExecutor<Exam> {

    boolean existsByExamId(String examId);

    Page<Exam> findAllByCreatorIdAndEnabledTrue(UUID creatorId, Pageable pageable);

    boolean existsByExamIdAndCreatorId(String examId, UUID creatorId);

    boolean existsByExamIdAndCreatorIdAndEnabled(String examId, UUID creatorId, Boolean enabled);
}
