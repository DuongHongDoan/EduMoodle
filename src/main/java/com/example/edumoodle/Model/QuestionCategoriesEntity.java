package com.example.edumoodle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "tbl_question_categories")
public class QuestionCategoriesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tùy chọn

    private int id;
    private String name;
    private int contextid;
    private String info;
    private int parent;
    private int moodle_id;
    private int courseid;

    public QuestionCategoriesEntity() {}
    public QuestionCategoriesEntity(String name, String info, int parent, int courseid) {
        this.name = name;
        this.info = info;
        this.parent = parent;
        this.courseid = courseid;
    }
    // Getters và Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMoodleId() {
        return moodle_id;
    }

    public void setMoodleId(int moodle_id) {
        this.moodle_id = moodle_id;
    }

    public int getCourseId() {
        return courseid;
    }

    public void setCourseId(int courseid) {
        this.courseid = courseid;
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

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Column(name = "category_id")
    private int categoryId;
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
}
