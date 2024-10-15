package com.example.edumoodle.DTO;

import java.util.List;

public class SectionsDTO {
    private Integer id;
    private String name;
    private Integer section;
    private List<Sections_ModuleDTO> modules;
    private List<ModuleDTO> moduless;
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

    public Integer getSection() {
        return section;
    }

    public void setSection(Integer section) {
        this.section = section;
    }

    public List<Sections_ModuleDTO> getModules() {
        return modules;
    }

    public void setModules(List<Sections_ModuleDTO> modules) {
        this.modules = modules;
    }

    public List<ModuleDTO> getModuless() {
        return moduless;
    }

    public void setModuless(List<ModuleDTO> moduless) {
        this.moduless = moduless;
    }
}

