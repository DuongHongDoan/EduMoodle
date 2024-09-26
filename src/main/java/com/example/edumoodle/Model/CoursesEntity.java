package com.example.edumoodle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "tbl_courses")
public class CoursesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_courses;

    @NotNull
    @Column(unique = true)
    private Integer moodleId;

    @NotNull
    private String fullname;

    @NotNull
    private String shortname;

    private String summary;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "categoryId", referencedColumnName = "moodleId")
    private CategoriesEntity categoriesEntity;

    public CoursesEntity() {}

    public CoursesEntity(Integer id_courses, Integer moodleId, String fullname, String shortname, String summary, CategoriesEntity categoriesEntity) {
        this.id_courses = id_courses;
        this.moodleId = moodleId;
        this.fullname = fullname;
        this.shortname = shortname;
        this.summary = summary;
        this.categoriesEntity = categoriesEntity;
    }

    public Integer getId_courses() {
        return id_courses;
    }

    public void setId_courses(Integer id_courses) {
        this.id_courses = id_courses;
    }

    public @NotNull Integer getMoodleId() {
        return moodleId;
    }

    public void setMoodleId(@NotNull Integer moodleId) {
        this.moodleId = moodleId;
    }

    public @NotNull String getFullname() {
        return fullname;
    }

    public void setFullname(@NotNull String fullname) {
        this.fullname = fullname;
    }

    public @NotNull String getShortname() {
        return shortname;
    }

    public void setShortname(@NotNull String shortname) {
        this.shortname = shortname;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public CategoriesEntity getCategoriesEntity() {
        return categoriesEntity;
    }

    public void setCategoriesEntity(CategoriesEntity categoriesEntity) {
        this.categoriesEntity = categoriesEntity;
    }
}
