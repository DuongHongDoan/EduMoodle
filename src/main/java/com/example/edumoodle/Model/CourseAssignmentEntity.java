package com.example.edumoodle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "tbl_CourseAssignment")
public class CourseAssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_course_assign;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "userRole_id", referencedColumnName = "id")
    private UserRoleEntity userRoleEntity;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "courseGroup_id", referencedColumnName = "id_course_group")
    private CourseGroupsEntity courseGroupsEntity;

    public CourseAssignmentEntity() {}

    public CourseAssignmentEntity(Integer id_course_assign, UserRoleEntity userRoleEntity, CourseGroupsEntity courseGroupsEntity) {
        this.id_course_assign = id_course_assign;
        this.userRoleEntity = userRoleEntity;
        this.courseGroupsEntity = courseGroupsEntity;
    }

    public Integer getId_course_assign() {
        return id_course_assign;
    }

    public void setId_course_assign(Integer id_course_assign) {
        this.id_course_assign = id_course_assign;
    }

    public @NotNull UserRoleEntity getUserRoleEntity() {
        return userRoleEntity;
    }

    public void setUserRoleEntity(@NotNull UserRoleEntity userRoleEntity) {
        this.userRoleEntity = userRoleEntity;
    }

    public @NotNull CourseGroupsEntity getCourseGroupsEntity() {
        return courseGroupsEntity;
    }

    public void setCourseGroupsEntity(@NotNull CourseGroupsEntity courseGroupsEntity) {
        this.courseGroupsEntity = courseGroupsEntity;
    }
}
