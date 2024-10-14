package com.example.edumoodle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
@Table(name = "tbl_SchoolYear_Semester")
public class SchoolYearSemesterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_schoolYear_semester;

    @OneToMany(mappedBy = "schoolYearSemesterEntity")
    private List<CourseGroupsEntity> courseGroups;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "schoolYear_id", referencedColumnName = "id_school_year")
    private SchoolYearsEntity schoolYearsEntity;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "semester_id", referencedColumnName = "id_semester")
    private SemestersEntity semestersEntity;

    public SchoolYearSemesterEntity() {}

    public SchoolYearSemesterEntity(Integer id_schoolYear_semester, SchoolYearsEntity schoolYearsEntity, SemestersEntity semestersEntity) {
        this.id_schoolYear_semester = id_schoolYear_semester;
        this.schoolYearsEntity = schoolYearsEntity;
        this.semestersEntity = semestersEntity;
    }

    public Integer getId_schoolYear_semester() {
        return id_schoolYear_semester;
    }

    public void setId_schoolYear_semester(Integer id_schoolYear_semester) {
        this.id_schoolYear_semester = id_schoolYear_semester;
    }

    public @NotNull SchoolYearsEntity getSchoolYearsEntity() {
        return schoolYearsEntity;
    }

    public void setSchoolYearsEntity(@NotNull SchoolYearsEntity schoolYearsEntity) {
        this.schoolYearsEntity = schoolYearsEntity;
    }

    public @NotNull SemestersEntity getSemestersEntity() {
        return semestersEntity;
    }

    public void setSemestersEntity(@NotNull SemestersEntity semestersEntity) {
        this.semestersEntity = semestersEntity;
    }
}
