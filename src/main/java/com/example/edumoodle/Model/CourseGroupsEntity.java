package com.example.edumoodle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
@Table(name = "tbl_CourseGroups")
public class CourseGroupsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_course_group;

    @NotNull
    private String groupName;

    @NotNull
    private String courseCode;

    @OneToMany(mappedBy = "courseGroupsEntity")
    private List<CourseAssignmentEntity> courseAssign;

    @OneToMany(mappedBy = "courseGroupsEntity")
    private List<ScheduleEntity> schedule;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "schoolYearSemester_id", referencedColumnName = "id_schoolYear_semester")
    private SchoolYearSemesterEntity schoolYearSemesterEntity;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "courses_id", referencedColumnName = "id_courses")
    private CoursesEntity coursesEntity;

    public CourseGroupsEntity() {}

    public CourseGroupsEntity(Integer id_course_group, String groupName, String courseCode, List<CourseAssignmentEntity> courseAssign, List<ScheduleEntity> schedule, SchoolYearSemesterEntity schoolYearSemesterEntity, CoursesEntity coursesEntity) {
        this.id_course_group = id_course_group;
        this.groupName = groupName;
        this.courseCode = courseCode;
        this.courseAssign = courseAssign;
        this.schedule = schedule;
        this.schoolYearSemesterEntity = schoolYearSemesterEntity;
        this.coursesEntity = coursesEntity;
    }

    public Integer getId_course_group() {
        return id_course_group;
    }

    public void setId_course_group(Integer id_course_group) {
        this.id_course_group = id_course_group;
    }

    public @NotNull String getGroupName() {
        return groupName;
    }

    public void setGroupName(@NotNull String groupName) {
        this.groupName = groupName;
    }

    public @NotNull String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(@NotNull String courseCode) {
        this.courseCode = courseCode;
    }

    public List<CourseAssignmentEntity> getCourseAssign() {
        return courseAssign;
    }

    public void setCourseAssign(List<CourseAssignmentEntity> courseAssign) {
        this.courseAssign = courseAssign;
    }

    public List<ScheduleEntity> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<ScheduleEntity> schedule) {
        this.schedule = schedule;
    }

    public @NotNull SchoolYearSemesterEntity getSchoolYearSemesterEntity() {
        return schoolYearSemesterEntity;
    }

    public void setSchoolYearSemesterEntity(@NotNull SchoolYearSemesterEntity schoolYearSemesterEntity) {
        this.schoolYearSemesterEntity = schoolYearSemesterEntity;
    }

    public @NotNull CoursesEntity getCoursesEntity() {
        return coursesEntity;
    }

    public void setCoursesEntity(@NotNull CoursesEntity coursesEntity) {
        this.coursesEntity = coursesEntity;
    }
}
