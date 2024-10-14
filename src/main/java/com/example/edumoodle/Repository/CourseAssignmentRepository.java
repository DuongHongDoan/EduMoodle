package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.CourseAssignmentEntity;
import com.example.edumoodle.Model.CourseGroupsEntity;
import com.example.edumoodle.Model.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseAssignmentRepository extends JpaRepository<CourseAssignmentEntity, Integer> {
    CourseAssignmentEntity findByCourseGroupsEntity(CourseGroupsEntity courseGroupsEntity);
    CourseAssignmentEntity findByCourseGroupsEntityAndUserRoleEntity(CourseGroupsEntity courseGroupsEntity, UserRoleEntity userRoleEntity);
}
