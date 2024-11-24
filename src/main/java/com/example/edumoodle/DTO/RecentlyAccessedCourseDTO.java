package com.example.edumoodle.DTO;

import java.time.LocalDateTime;

public class RecentlyAccessedCourseDTO {

    private Integer userId;
    private Integer courseId;
    private LocalDateTime accessedAt;
    private String courseName;     // Tên khóa học
    private String categoryName;   // Tên danh mục
    private String instructorName; // Tên giảng viên

    public RecentlyAccessedCourseDTO(Integer userId, Integer courseId, LocalDateTime accessedAt, String courseName, String categoryName, String instructorName) {
        this.userId = userId;
        this.courseId = courseId;
        this.accessedAt = accessedAt;
        this.courseName = courseName;
        this.categoryName = categoryName;
        this.instructorName = instructorName;
    }

    // Getters and setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public LocalDateTime getAccessedAt() {
        return accessedAt;
    }

    public void setAccessedAt(LocalDateTime accessedAt) {
        this.accessedAt = accessedAt;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }


}

