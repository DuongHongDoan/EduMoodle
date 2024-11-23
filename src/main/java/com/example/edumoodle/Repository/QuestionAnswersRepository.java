package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.QuestionAnswersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionAnswersRepository extends JpaRepository<QuestionAnswersEntity, Integer> {
}
