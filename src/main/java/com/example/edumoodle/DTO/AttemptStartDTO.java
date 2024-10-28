package com.example.edumoodle.DTO;

public class AttemptStartDTO {
    private Integer attemptId;
    private Integer quizId;

    public AttemptStartDTO(Integer attemptId, Integer quizId) {
        this.attemptId = attemptId;
        this.quizId = quizId;
    }

    // Getters and Setters
    public Integer getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Integer attemptId) {
        this.attemptId = attemptId;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public void setQuizId(Integer quizId) {
        this.quizId = quizId;
    }
}
