package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.CategoriesEntity;
import com.example.edumoodle.Model.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<UsersEntity, Integer> {

    boolean existsByMoodleId(Integer moodleId);
    Optional<UsersEntity> findByMoodleId(Integer moodleId);
    UsersEntity findByUsername(String username);
    void deleteById(int id);
}
