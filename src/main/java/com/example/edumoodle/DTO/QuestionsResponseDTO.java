package com.example.edumoodle.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class QuestionsResponseDTO {
    private List<QuestionsDTO> questions;

    // Getters and Setters
    public List<QuestionsDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionsDTO> questions) {
        this.questions = questions;
    }
}
