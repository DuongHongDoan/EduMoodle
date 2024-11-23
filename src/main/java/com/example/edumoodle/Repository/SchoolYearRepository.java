package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.SchoolYearsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolYearRepository extends JpaRepository<SchoolYearsEntity, Integer> {
    SchoolYearsEntity findBySchoolYearName(String schoolYearName);
}
