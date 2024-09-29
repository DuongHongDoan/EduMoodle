package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.CategoriesEntity;
import com.example.edumoodle.Model.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<UsersEntity, Integer> {

    boolean existsByMoodleId(Integer moodleId);
    Optional<UsersEntity> findByMoodleId(Integer moodleId);
    UsersEntity findByUsername(String username);
    void deleteById(int id);

    //lấy danh sách user có role = admin
    @Query("SELECT u FROM UsersEntity u JOIN u.userRole ur JOIN ur.rolesEntity r WHERE r.name = :roleName")
    List<UsersEntity> findUsersByRoleName(@Param("roleName") String roleName);

    // Lấy danh sách user không có vai trò ADMIN
    @Query("SELECT u FROM UsersEntity u WHERE u.id_user NOT IN " +
            "(SELECT ur.usersEntity.id_user FROM UserRoleEntity ur JOIN ur.rolesEntity r WHERE r.name = 'ADMIN')")
    List<UsersEntity> findUsersWithoutAdminRole();
}
