package com.example.edumoodle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "tbl_DanhMuc")
public class CategoriesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer moodleId;

    @NotNull(message = "Tên danh mục không được để trống.")
    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String description;

    @NotNull(message = "Danh mục gốc không được để trống.")
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer parent = 0;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer coursecount = 0;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMoodleId() {
        return moodleId;
    }

    public void setMoodleId(Integer moodleId) {
        this.moodleId = moodleId;
    }

    public @NotNull(message = "Tên danh mục không được để trống.") String getName() {
        return name;
    }

    public void setName(@NotNull(message = "Tên danh mục không được để trống.") String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public @NotNull(message = "Danh mục gốc không được để trống.") Integer getParent() {
        return parent;
    }

    public void setParent(@NotNull(message = "Danh mục gốc không được để trống.") Integer parent) {
        this.parent = parent;
    }

    public Integer getCoursecount() {
        return coursecount;
    }

    public void setCoursecount(Integer coursecount) {
        this.coursecount = coursecount;
    }
}
