package com.example.edumoodle.DTO;

import java.util.List;

public class CoursesDTO {
    private Integer id;
    private Integer moodleCourseId;
    private Integer categoryid;
    private String fullname;
    private String shortname;
    private String summary;
    private String description;
    private String categoryName;
    private String teacherName;
    private List<SectionsDTO> sections; // Danh sách các section trong khóa học

    public CoursesDTO() {}

    public CoursesDTO(Integer categoryid, String fullname, String shortname, String summary) {
        this.categoryid = categoryid;
        this.fullname = fullname;
        this.shortname = shortname;
        this.summary = summary;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMoodleCourseId() {
        return moodleCourseId;
    }

    public void setMoodleCourseId(Integer moodleCourseId) {
        this.moodleCourseId = moodleCourseId;
    }

    public Integer getCategoryid() {
        return categoryid;
    }

    public void setCategoryid(Integer categoryid) {
        this.categoryid = categoryid;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public List<SectionsDTO> getSections() {
        return sections;
    }

    public void setSections(List<SectionsDTO> sections) {
        this.sections = sections;
    }
}
