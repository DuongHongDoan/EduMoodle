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
}
