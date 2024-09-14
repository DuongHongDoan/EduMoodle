package com.example.edumoodle.DTO;

public class CategoriesDTO {
    private Integer id;
    private String name;
    private String description;
    private Integer parent;
    private Integer coursecount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public Integer getCoursecount() {
        return coursecount;
    }

    public void setCoursecount(Integer coursecount) {
        this.coursecount = coursecount;
    }
}
