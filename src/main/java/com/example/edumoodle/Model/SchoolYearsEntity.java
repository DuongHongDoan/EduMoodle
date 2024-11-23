package com.example.edumoodle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
@Table(name = "tbl_SchoolYears")
public class SchoolYearsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_school_year;

    @NotNull
    private String schoolYearName;

    @OneToMany(mappedBy = "schoolYearsEntity")
    private List<SchoolYearSemesterEntity> schoolYearSemester;

    public SchoolYearsEntity() {}

    public SchoolYearsEntity(Integer id_school_year, String schoolYearName, List<SchoolYearSemesterEntity> schoolYearSemester) {
        this.id_school_year = id_school_year;
        this.schoolYearName = schoolYearName;
        this.schoolYearSemester = schoolYearSemester;
    }

    public Integer getId_school_year() {
        return id_school_year;
    }

    public void setId_school_year(Integer id_school_year) {
        this.id_school_year = id_school_year;
    }

    public @NotNull String getSchoolYearName() {
        return schoolYearName;
    }

    public void setSchoolYearName(@NotNull String schoolYearName) {
        this.schoolYearName = schoolYearName;
    }

    public List<SchoolYearSemesterEntity> getSchoolYearSemester() {
        return schoolYearSemester;
    }

    public void setSchoolYearSemester(List<SchoolYearSemesterEntity> schoolYearSemester) {
        this.schoolYearSemester = schoolYearSemester;
    }
}
