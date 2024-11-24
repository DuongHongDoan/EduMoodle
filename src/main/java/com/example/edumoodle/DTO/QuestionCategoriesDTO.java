package com.example.edumoodle.DTO;

import java.util.ArrayList;
import java.util.List;

public class QuestionCategoriesDTO {
    private int id;
    private String name;
    private int contextid;
    private String info;
    private Integer parent;
    private  int moodle_id;
    private  int courseid;
    private int questionCount;
    private List<QuestionCategoriesDTO> children = new ArrayList<>();

    // Getters và Setters
    public List<QuestionCategoriesDTO> getChildren() {
        return children;
    }
    public void setChildren(List<QuestionCategoriesDTO> children) {
        this.children = children;
    }
    public QuestionCategoriesDTO toEntity() {
        QuestionCategoriesDTO entity = new QuestionCategoriesDTO();
        entity.setId(this.id);
        entity.setName(this.name);
        entity.setInfo(this.info);
        entity.setParent(this.parent);
        entity.setMoodleId(this.moodle_id);
        entity.setCourseId(this.courseid);
        return entity;
    }


    // Constructor mặc định (bắt buộc với Jackson)
    public QuestionCategoriesDTO() {
    }

    // Constructor có tham số (tùy chọn)
    public QuestionCategoriesDTO(int id, String name, String info, int parent) {
        this.id = id;
        this.name = name;
        this.info = info;
        this.parent=parent;
    }
    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }
    public Integer getCourseId() {
        return courseid;
    }

    public void setCourseId(Integer courseid) {
        this.courseid = courseid;
    }
    // Getters và Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getContextid() {
        return contextid;
    }

    public void setContextid(int contextid) {
        this.contextid = contextid;
    }
    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public Integer getParent() {
        return parent;
    }



    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
    public int getMoodleId() {
        return moodle_id;
    }

    public void setMoodleId(int moodle_id) {
        this.moodle_id = moodle_id;
    }


}
