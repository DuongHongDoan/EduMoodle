package com.example.edumoodle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
@Table(name = "tbl_semester")
public class SemestersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_semester;

    @NotNull
    private String semesterName;

    @OneToMany(mappedBy = "semestersEntity")
    private List<SchoolYearSemesterEntity> semesterSchoolYear;

    public SemestersEntity() {}

    public SemestersEntity(Integer id_semester, String semesterName, List<SchoolYearSemesterEntity> semesterSchoolYear) {
        this.id_semester = id_semester;
        this.semesterName = semesterName;
        this.semesterSchoolYear = semesterSchoolYear;
    }

    public Integer getId_semester() {
        return id_semester;
    }

    public void setId_semester(Integer id_semester) {
        this.id_semester = id_semester;
    }

    public @NotNull String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(@NotNull String semesterName) {
        this.semesterName = semesterName;
    }

    public List<SchoolYearSemesterEntity> getSemesterSchoolYear() {
        return semesterSchoolYear;
    }

    public void setSemesterSchoolYear(List<SchoolYearSemesterEntity> semesterSchoolYear) {
        this.semesterSchoolYear = semesterSchoolYear;
    }
}
