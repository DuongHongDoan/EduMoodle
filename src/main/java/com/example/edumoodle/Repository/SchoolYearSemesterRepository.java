package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.SchoolYearSemesterEntity;
import com.example.edumoodle.Model.SchoolYearsEntity;
import com.example.edumoodle.Model.SemestersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SchoolYearSemesterRepository extends JpaRepository<SchoolYearSemesterEntity, Integer> {
    SchoolYearSemesterEntity findBySchoolYearsEntityAndSemestersEntity(SchoolYearsEntity schoolYearName, SemestersEntity semesterName);

}
