package com.example.edumoodle.DTO;

public class QuestionAnswersDTO {
    private int id;
    private String answerText;
    private boolean correct;

    // Constructors
    public QuestionAnswersDTO() {
    }

    public QuestionAnswersDTO(int id, String answerText, boolean correct) {
        this.id = id;
        this.answerText = answerText;
        this.correct = correct;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
