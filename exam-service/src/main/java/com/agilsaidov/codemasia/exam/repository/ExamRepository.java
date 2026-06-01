package com.agilsaidov.codemasia.exam.repository;

import com.agilsaidov.codemasia.exam.model.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExamRepository extends JpaRepository<Exam, String> {

    boolean existsByExamId(String examId);

    Page<Exam> findAllByCreatorId(UUID creatorId, Pageable pageable);
}
