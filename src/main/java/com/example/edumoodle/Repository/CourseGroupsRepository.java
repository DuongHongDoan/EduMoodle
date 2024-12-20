package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.CourseGroupsEntity;
import com.example.edumoodle.Model.CoursesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseGroupsRepository extends JpaRepository<CourseGroupsEntity, Integer> {
    CourseGroupsEntity findByCoursesEntity(CoursesEntity coursesEntity);
    List<CourseGroupsEntity> findAllByCoursesEntity(CoursesEntity coursesEntity);
    CourseGroupsEntity findByGroupName(String groupName);
}
