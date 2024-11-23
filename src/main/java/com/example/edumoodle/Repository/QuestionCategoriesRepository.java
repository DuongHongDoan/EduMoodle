package com.example.edumoodle.Repository;

import com.example.edumoodle.DTO.QuestionCategoriesDTO;
import com.example.edumoodle.Model.QuestionCategoriesEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionCategoriesRepository extends JpaRepository<QuestionCategoriesEntity, Integer> {

        List<QuestionCategoriesEntity> findAll();

        Page<QuestionCategoriesEntity> findByCourseId(int courseId, Pageable pageable);

        @Transactional
        @Modifying
        @Query("DELETE FROM QuestionCategoriesEntity q WHERE q.moodleId = :moodle_id")
        void deleteByMoodleId(@Param("moodle_id") int moodle_id);

        @Modifying
        @Transactional
        @Query("UPDATE QuestionCategoriesEntity q SET q.name = :name, q.contextid = :contextId, q.info = :info, q.parent = :parent WHERE q.moodleId = :moodleId")
        int updateCategoryByMoodleId(@Param("name") String name,
                                     @Param("contextId") int contextId,
                                     @Param("info") String info,
                                     @Param("parent") int parent,
                                     @Param("moodleId") int moodleId);

        Optional<QuestionCategoriesEntity> findByMoodleId(int moodleId);

        // Phương thức tìm danh mục con theo parent
        List<QuestionCategoriesEntity> findByParent(int parent);
}
