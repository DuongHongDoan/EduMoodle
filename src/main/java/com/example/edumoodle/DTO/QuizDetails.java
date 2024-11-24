package com.example.edumoodle.DTO;

public class QuizDetails {
    private Double maxGrade;
    private Integer numberOfQuestions;

    public QuizDetails(Double maxGrade, Integer numberOfQuestions) {
        this.maxGrade = maxGrade;
        this.numberOfQuestions = numberOfQuestions;
    }

    public Double getMaxGrade() {
        return maxGrade;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }
}
