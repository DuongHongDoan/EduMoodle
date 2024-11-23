package com.example.edumoodle.Mapper;

import com.example.edumoodle.DTO.QuestionCategoriesDTO;
import com.example.edumoodle.Model.QuestionCategoriesEntity;
import org.springframework.stereotype.Component;

@Component
public class QuestionCategoryMapper {

    // Chuyển từ Entity sang DTO
    public QuestionCategoriesDTO toDTO(QuestionCategoriesEntity entity) {
        QuestionCategoriesDTO dto = new QuestionCategoriesDTO();
        dto.setId(entity.getId());
        dto.setMoodleId(entity.getMoodleId());
        dto.setParent(entity.getParent());
        dto.setName(entity.getName());
        return dto;
    }

    // Chuyển từ DTO sang Entity
    public QuestionCategoriesEntity toEntity(QuestionCategoriesDTO dto) {
        QuestionCategoriesEntity entity = new QuestionCategoriesEntity();
        entity.setId(dto.getId());
        entity.setMoodleId(dto.getMoodleId());
        entity.setParent(dto.getParent());
        entity.setName(dto.getName());
        return entity;
    }
}
