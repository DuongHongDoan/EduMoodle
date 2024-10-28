package com.example.edumoodle.DTO;

public class StudentsCourseDTO {
    private Integer userId;
    private String fullName;
    private String firstName;  // Thêm firstname
    private String lastName;   // Thêm lastname
    private String role;
    private Long lastCourseAccess;

    // Constructor bao gồm firstname và lastname
    public StudentsCourseDTO(Integer userId, String fullName, String firstName, String lastName, String role, Long lastCourseAccess) {
        this.userId = userId;
        this.fullName = fullName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.lastCourseAccess = lastCourseAccess;
    }

    // Getters và Setters

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getLastCourseAccess() {
        return lastCourseAccess;
    }

    public void setLastCourseAccess(Long lastCourseAccess) {
        this.lastCourseAccess = lastCourseAccess;
    }
}
