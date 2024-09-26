package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.CoursesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoursesRepository extends JpaRepository<CoursesEntity, Integer> {
//    CoursesEntity findByMoodleCourseId(Integer moodleCourseId);
}
