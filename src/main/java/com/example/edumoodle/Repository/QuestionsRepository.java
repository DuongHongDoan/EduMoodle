package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.QuestionAnswersEntity;
import com.example.edumoodle.Model.QuestionsEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface QuestionsRepository extends JpaRepository<QuestionsEntity, Integer> {
    void deleteByMoodleId(int moodleId);

    Optional<QuestionsEntity> findById(int id);
    List<QuestionsEntity> findByCategoryId(QuestionCategoriesRepository categoryId);
    @Query("SELECT a FROM QuestionAnswersEntity a WHERE a.id = :answerId")
    Optional<QuestionAnswersEntity> findAnswerById(int answerId);
//    Optional<QuestionCategoriesEntity> findByMoodleId(int moodleId);
    @Transactional
    @Modifying
    @Query("DELETE FROM QuestionsEntity q WHERE q.id = :id")
    int deleteById(@Param("id") int id);

//    @Modifying
//    @Query("DELETE FROM QuestionsEntity q WHERE q.moodleId = :moodleId")
//    int deleteByMoodleId(@Param("moodleId") int moodleId);

//    @Query("DELETE FROM QuestionCategoriesEntity q WHERE q.moodleId = :moodle_id")
//    void deleteByMoodleId(@Param("moodle_id") int moodle_id);


    @Modifying
    @Query("UPDATE QuestionsEntity q SET q.name = :name, q.questionText = :questionText WHERE q.moodleId = :moodleId")
    int updateQuestionByMoodleId(@Param("name") String name,
                                 @Param("questionText") String questionText,
//                                 @Param("correctAnswerIndex") int correctAnswerIndex,
                                 @Param("moodleId") int moodleId);

    // Phương thức tìm câu hỏi theo moodleId
    Optional<QuestionsEntity> findByMoodleId(int moodleId);
}
