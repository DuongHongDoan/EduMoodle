package com.example.edumoodle.DTO;

public class QuizDetails {
    private Double maxGrade;
    private Integer numberOfQuestions;
    private String quizName; // Thêm trường này để lưu tên bài kiểm tra

    public QuizDetails(Double maxGrade, Integer numberOfQuestions, String quizName) {
        this.maxGrade = maxGrade;
        this.numberOfQuestions = numberOfQuestions;
        this.quizName = quizName;
    }

    // Getter và Setter cho quizName
    public String getQuizName() {
        return quizName;
    }

    public void setQuizName(String quizName) {
        this.quizName = quizName;
    }

    // Các getter và setter khác


    public Double getMaxGrade() {
        return maxGrade;
    }

    public void setMaxGrade(Double maxGrade) {
        this.maxGrade = maxGrade;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }
}

