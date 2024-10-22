package com.example.edumoodle.DTO;

public class QuizDTO {
    private Integer id;
    private String name;
    private Integer moduleId; // Thêm thuộc tính moduleId

    // Constructor
    public QuizDTO(Integer id, String name, Integer moduleId) {
        this.id = id;
        this.name = name;
        this.moduleId = moduleId;
    }

    // Getters và setters cho moduleId
    public Integer getModuleId() {
        return moduleId;
    }

    public void setModuleId(Integer moduleId) {
        this.moduleId = moduleId;
    }

    // Các getters và setters khác

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
}

