package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.CoursesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoursesRepository extends JpaRepository<CoursesEntity, Integer> {
    Optional<CoursesEntity> findByMoodleId(Integer moodleCourseId);

    @Query("SELECT c FROM CoursesEntity c " +
            "JOIN c.courseGroupsEntities nh " +
            "JOIN nh.schoolYearSemesterEntity nhhk " +
            "WHERE nhhk.schoolYearsEntity.id_school_year = :id_school_year AND nhhk.semestersEntity.id_semester = :id_semester")
    List<CoursesEntity> findCoursesBySchoolYearSemester(@Param("id_school_year") Integer id_school_year,
                                                        @Param("id_semester") Integer id_semester);
}
