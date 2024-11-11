package com.example.edumoodle.DTO;

import java.util.List;

public class QuestionDetail {
    private int questionNumber;
    private String questionText;
    private String studentResponse;
    private String correctResponse;
    private List<String> allResponses;  // Danh sách tất cả các phương án
    private String html; // HTML của câu hỏi
    private boolean isCorrect; // Trạng thái đúng/sai

    // Constructor mặc định
    public QuestionDetail() {
    }

    // Constructor đầy đủ
    public QuestionDetail(int questionNumber, String questionText, String studentResponse, String correctResponse, List<String> allResponses, String html, boolean isCorrect) {
        this.questionNumber = questionNumber; // Khởi tạo số câu hỏi
        this.questionText = questionText;
        this.studentResponse = studentResponse;
        this.correctResponse = correctResponse;
        this.allResponses = allResponses; // Khởi tạo danh sách tất cả các phương án
        this.html = html;
        this.isCorrect = isCorrect;
    }

    // Getter và Setter
    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getStudentResponse() {
        return studentResponse;
    }

    public void setStudentResponse(String studentResponse) {
        this.studentResponse = studentResponse;
    }

    public String getCorrectResponse() {
        return correctResponse;
    }

    public void setCorrectResponse(String correctResponse) {
        this.correctResponse = correctResponse;
    }

    public List<String> getAllResponses() {
        return allResponses;
    }

    public void setAllResponses(List<String> allResponses) {
        this.allResponses = allResponses;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
}
