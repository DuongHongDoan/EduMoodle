package com.example.edumoodle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "tbl_schedule")
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_schedule;

    private String NgayHoc;
    private String TietHoc;
    private String TuanHoc;
    private String PhongHoc;
    private String PhuongThucHoc;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "courseGroup_id", referencedColumnName = "id_course_group")
    private CourseGroupsEntity courseGroupsEntity;
}
