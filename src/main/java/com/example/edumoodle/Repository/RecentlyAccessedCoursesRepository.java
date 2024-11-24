package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.RecentlyAccessedCoursesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecentlyAccessedCoursesRepository extends JpaRepository<RecentlyAccessedCoursesEntity, Integer> {

    // Change return type to Optional to handle the case where the record may not exist
    Optional<RecentlyAccessedCoursesEntity> findByUserIdAndCourseId(Integer userId, Integer courseId);

    // Retrieve recently accessed courses for a user, ordered by access time in descending order
    List<RecentlyAccessedCoursesEntity> findByUserIdOrderByAccessedAtDesc(Integer userId);
}
