package com.example.edumoodle.Model;

import lombok.Data;

@Data
public class CategoriesModel {
    private Integer id;
    private String name;
    private String description;
    private Integer parent;
    private Integer coursecount;
}
