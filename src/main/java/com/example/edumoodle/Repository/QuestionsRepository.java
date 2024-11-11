package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.CoursesEntity;
import com.example.edumoodle.Model.QuestionAnswersEntity;
import com.example.edumoodle.Model.QuestionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface QuestionsRepository extends JpaRepository<QuestionsEntity, Integer> {
    List<QuestionsEntity> findByCategoryId(Long categoryId);
    @Query("SELECT a FROM QuestionAnswersEntity a WHERE a.id = :answerId")
    Optional<QuestionAnswersEntity> findAnswerById(int answerId);
}
