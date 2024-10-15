package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.CoursesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoursesRepository extends JpaRepository<CoursesEntity, Integer> {
    Optional<CoursesEntity> findByMoodleId(Integer moodleCourseId);

}
