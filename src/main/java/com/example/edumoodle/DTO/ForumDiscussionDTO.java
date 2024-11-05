package com.example.edumoodle.DTO;

public class ForumDiscussionDTO {
    private String subject;
    private String message;
    private String userFullName;

    // Constructor
    public ForumDiscussionDTO(String subject, String message, String userFullName) {
        this.subject = subject;
        this.message = message;
        this.userFullName = userFullName;
    }

    // Getters
    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public String getUserFullName() {
        return userFullName;
    }
}
