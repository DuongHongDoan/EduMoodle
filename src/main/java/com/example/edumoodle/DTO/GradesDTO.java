package com.example.edumoodle.DTO;

import java.util.List;

public class GradesDTO {
    private Integer userId;
    private Integer moodleCourseId;
    private String courseName; // New field for course name
    private List<GradeItemDTO> gradeItems;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getMoodleCourseId() {
        return moodleCourseId;
    }

    public void setMoodleCourseId(Integer moodleCourseId) {
        this.moodleCourseId = moodleCourseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public List<GradeItemDTO> getGradeItems() {
        return gradeItems;
    }

    public void setGradeItems(List<GradeItemDTO> gradeItems) {
        this.gradeItems = gradeItems;
    }
}
