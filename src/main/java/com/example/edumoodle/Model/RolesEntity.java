package com.example.edumoodle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

@Entity
@Table(name = "tbl_roles")
public class RolesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_role;

    @NotNull
    @Column(unique = true)
    private Integer moodleId;

    @NotNull
    private String name;

    @OneToMany(mappedBy = "rolesEntity")
    private Set<UserRoleEntity> roleUser;

    public RolesEntity(){}

    public RolesEntity(Integer id_role, Integer moodleId, String name) {
        this.id_role = id_role;
        this.moodleId = moodleId;
        this.name = name;
    }

    public RolesEntity(Integer id_role, Integer moodleId, String name, Set<UserRoleEntity> roleUser) {
        this.id_role = id_role;
        this.moodleId = moodleId;
        this.name = name;
        this.roleUser = roleUser;
    }

    public Set<UserRoleEntity> getRoleUser() {
        return roleUser;
    }

    public void setRoleUser(Set<UserRoleEntity> roleUser) {
        this.roleUser = roleUser;
    }

    public Integer getId_role() {
        return id_role;
    }

    public void setId_role(Integer id_role) {
        this.id_role = id_role;
    }

    public @NotNull Integer getMoodleId() {
        return moodleId;
    }

    public void setMoodleId(@NotNull Integer moodleId) {
        this.moodleId = moodleId;
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }
}
