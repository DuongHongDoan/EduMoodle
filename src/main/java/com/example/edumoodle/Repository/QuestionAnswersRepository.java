package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.QuestionAnswersEntity;
import com.example.edumoodle.Model.QuestionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionAnswersRepository extends JpaRepository<QuestionAnswersEntity, Integer> {
    // Xóa đáp án theo question_id
    void deleteByQuestion_MoodleId(int moodleId);

    // Hoặc xóa tất cả các đáp án liên quan đến một câu hỏi
    void deleteByQuestion(QuestionsEntity question);
}
