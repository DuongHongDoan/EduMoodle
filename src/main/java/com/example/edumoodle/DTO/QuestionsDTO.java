package com.example.edumoodle.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class QuestionsDTO {
    private Integer categoryId;
    private String name;
    private String moodleId;
    private String questionText;
    private String qtype;
    private List<QuestionAnswersDTO> answers = new ArrayList<>(); // Khởi tạo danh sách answers
    private Integer correctAnswerIndex;
   



    // Getters and Setters

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getMoodleId() {
        return moodleId;
    }

    public void setMoodleId(String moodleId) {
        this.moodleId = moodleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQtype() {
        return qtype;
    }

    public void setQtype(String qtype) {
        this.qtype = qtype;
    }

    public List<QuestionAnswersDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<QuestionAnswersDTO> answers) {
        this.answers = answers != null ? answers : new ArrayList<>(); // Đảm bảo không có null
    }

    public Integer getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public void setCorrectAnswerIndex(Integer correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }
}
