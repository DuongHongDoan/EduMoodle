package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.RolesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolesRepository extends JpaRepository<RolesEntity, Integer> {
    // Truy vấn để tìm vai trò ADMIN
    @Query("SELECT r FROM RolesEntity r WHERE r.name = :roleName")
    Optional<RolesEntity> findRoleByName(@Param("roleName") String roleName);
    RolesEntity findByName(String name);
    Optional<RolesEntity> findByMoodleId(Integer moodleId);
}
