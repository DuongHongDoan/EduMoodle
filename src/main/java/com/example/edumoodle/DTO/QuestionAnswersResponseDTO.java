package com.example.edumoodle.DTO;

import java.util.List;

public class QuestionAnswersResponseDTO {
    private String status;
    private List<QuestionAnswersDTO> answers;

    // Constructors
    public QuestionAnswersResponseDTO() {
    }

    public QuestionAnswersResponseDTO(String status, List<QuestionAnswersDTO> answers) {
        this.status = status;
        this.answers = answers;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<QuestionAnswersDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<QuestionAnswersDTO> answers) {
        this.answers = answers;
    }
    private String question; // Hoặc có thể là một đối tượng của QuestionsEntity


    // Getter và Setter
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }


}
