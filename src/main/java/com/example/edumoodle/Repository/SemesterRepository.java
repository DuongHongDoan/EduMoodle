package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.SchoolYearsEntity;
import com.example.edumoodle.Model.SemestersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SemesterRepository extends JpaRepository<SemestersEntity, Integer> {
    SemestersEntity findBySemesterName(String semesterName);

}
