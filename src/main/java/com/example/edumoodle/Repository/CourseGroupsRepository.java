package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.CourseGroupsEntity;
import com.example.edumoodle.Model.CoursesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseGroupsRepository extends JpaRepository<CourseGroupsEntity, Integer> {
}
