package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.CategoriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriesRepository extends JpaRepository<CategoriesEntity, Integer> {
    boolean existsByMoodleId(Integer moodleId);
    CategoriesEntity findByMoodleId(Integer moodleId);

}
