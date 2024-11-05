package com.example.edumoodle.DTO;

import java.time.LocalDateTime;
import java.util.List;

public class AttemptIDTO {

    private Integer attemptId;
    private Double score;
    private Double maxGrade; // Điểm tối đa
    private Integer numberOfQuestions; // Tổng số câu hỏi
    private List<QuestionDetail> questionDetails;
    private LocalDateTime timeStart; // Thời gian bắt đầu
    private LocalDateTime timeFinish; // Thời gian kết thúc
    private String status; // Trạng thái

    public AttemptIDTO() {
        // Constructor mặc định
    }

    public AttemptIDTO(Integer attemptId, Double score, Double maxGrade, Integer numberOfQuestions,
                       LocalDateTime timeStart, LocalDateTime timeFinish, String status) {
        this.attemptId = attemptId;
        this.score = score;
        this.maxGrade = maxGrade;
        this.numberOfQuestions = numberOfQuestions;
        this.timeStart = timeStart;
        this.timeFinish = timeFinish;
        this.status = status;
    }

    public Integer getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Integer attemptId) {
        this.attemptId = attemptId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

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

    public List<QuestionDetail> getQuestionDetails() {
        return questionDetails;
    }

    public void setQuestionDetails(List<QuestionDetail> questionDetails) {
        this.questionDetails = questionDetails;
    }

    public LocalDateTime getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(LocalDateTime timeStart) {
        this.timeStart = timeStart;
    }

    public LocalDateTime getTimeFinish() {
        return timeFinish;
    }

    public void setTimeFinish(LocalDateTime timeFinish) {
        this.timeFinish = timeFinish;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
