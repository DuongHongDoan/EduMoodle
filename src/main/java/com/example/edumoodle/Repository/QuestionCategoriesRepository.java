package com.example.edumoodle.Repository;

import com.example.edumoodle.DTO.QuestionCategoriesDTO;
import com.example.edumoodle.Model.QuestionCategoriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionCategoriesRepository extends JpaRepository<QuestionCategoriesEntity, Integer> {
    List<QuestionCategoriesEntity> findAll();
//    Optional<QuestionCategoriesEntity> findByMoodleId(int moodle_id);
//    Optional<QuestionCategoriesEntity> findByMoodle_id(int moodle_id);
//    Optional<QuestionCategoriesEntity> findByMoodle_id(int moodle_id);
}
