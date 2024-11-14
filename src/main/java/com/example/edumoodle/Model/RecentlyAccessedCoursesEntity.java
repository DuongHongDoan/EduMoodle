package com.example.edumoodle.Model;

import com.example.edumoodle.DTO.RecentlyAccessedCourseDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
@Entity
@Table(name = "recently_accessed_courses")
public class RecentlyAccessedCoursesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(nullable = false)
    private Integer userId;

    @NotNull
    @Column(nullable = false)
    private Integer courseId;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime accessedAt;

    // Các trường mới cho thông tin khóa học
    private String courseName;     // Tên khóa học
    private String categoryName;   // Tên danh mục
    private String instructorName; // Tên giảng viên

    public RecentlyAccessedCoursesEntity() {}

    public RecentlyAccessedCoursesEntity(Integer userId, Integer courseId) {
        this.userId = userId;
        this.courseId = courseId;
    }

    public Integer getId() {
        return id;
    }

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

    // Automatically sets accessedAt to the current timestamp before persisting
    @PrePersist
    public void prePersist() {
        this.accessedAt = LocalDateTime.now();
    }

    // Getter and setter for the new fields
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

    // Convenience method to convert Entity to DTO
    public RecentlyAccessedCourseDTO toDTO() {
        return new RecentlyAccessedCourseDTO(userId, courseId, accessedAt, courseName, categoryName, instructorName);
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setAccessedAt(@NotNull LocalDateTime accessedAt) {
        this.accessedAt = accessedAt;
    }
}
