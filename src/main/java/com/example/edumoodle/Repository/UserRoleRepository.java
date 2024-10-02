package com.example.edumoodle.Repository;

import com.example.edumoodle.Model.RolesEntity;
import com.example.edumoodle.Model.UserRoleEntity;
import com.example.edumoodle.Model.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Integer> {
    List<UserRoleEntity> findByUsersEntity(UsersEntity usersEntity);
    // Xóa vai trò của người dùng
    @Modifying
    @Transactional
    @Query("DELETE FROM UserRoleEntity ur WHERE ur.usersEntity = :usersEntity AND ur.rolesEntity = :rolesEntity")
    void deleteByUsersEntityAndRolesEntity(@Param("usersEntity") UsersEntity usersEntity, @Param("rolesEntity") RolesEntity rolesEntity);
}

