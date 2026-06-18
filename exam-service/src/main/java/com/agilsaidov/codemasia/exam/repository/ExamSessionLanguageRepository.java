package com.agilsaidov.codemasia.exam.repository;

import com.agilsaidov.codemasia.exam.model.ExamSessionLanguage;
import com.agilsaidov.codemasia.exam.model.ExamSessionLanguageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamSessionLanguageRepository extends JpaRepository<ExamSessionLanguage, ExamSessionLanguageId> {
}
