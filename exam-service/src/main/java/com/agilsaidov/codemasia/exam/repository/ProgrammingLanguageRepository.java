package com.agilsaidov.codemasia.exam.repository;

import com.agilsaidov.codemasia.exam.dto.response.ProgrammingLanguageResponse;
import com.agilsaidov.codemasia.exam.model.ProgrammingLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgrammingLanguageRepository extends JpaRepository<ProgrammingLanguage, Long> {
    List<ProgrammingLanguage> findAllByEnabled(boolean enabled);

    Optional<ProgrammingLanguage> findByJudge0LanguageIdAndEnabledTrue(Integer judge0LanguageId);
}
