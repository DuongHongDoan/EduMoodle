package com.example.edumoodle.DTO;

import java.util.List;

public class CategoriesResponseDTO {
    private String status;
    private List<QuestionCategoriesDTO> categories;

    // Getters và Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<QuestionCategoriesDTO> getCategories() {
        return categories;
    }

    public void setCategories(List<QuestionCategoriesDTO> categories) {
        this.categories = categories;
    }
}
