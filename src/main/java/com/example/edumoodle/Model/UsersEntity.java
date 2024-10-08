package com.example.edumoodle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

@Entity
@Table (name = "tbl_User")
public class UsersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_user;
    @Column(unique = true)
    private Integer moodleId;

    @NotNull (message = "Không bỏ trống username")
    private String username;

    @NotNull (message = "Không bỏ trống firstname")
    private String firstname;

    @NotNull (message = "Không bỏ trống lastname")
    private String lastname;

    @NotNull (message = "Không bỏ trống password")
    private String password;

    @NotNull (message = "Không bỏ trống email")
    private String email;

    @OneToMany(mappedBy = "usersEntity", fetch = FetchType.EAGER)
    private Set<UserRoleEntity> userRole;


    public UsersEntity(){}

    public UsersEntity(Integer moodleId, String username, String firstname, String lastname, String password, String email) {
        this.moodleId = moodleId;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
        this.email = email;
    }

    public UsersEntity(Integer id_user, Integer moodleId, String username, String firstname, String lastname, String password, String email, Set<UserRoleEntity> userRoleEntities) {
        this.id_user = id_user;
        this.moodleId = moodleId;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
        this.email = email;
        this.userRole = userRoleEntities;
    }

    public Set<UserRoleEntity> getUserRole() {
        return userRole;
    }

    public void setUserRole(Set<UserRoleEntity> userRoleEntities) {
        this.userRole = userRoleEntities;
    }

    public Integer getId_user() {
        return id_user;
    }

    public void setId_user(Integer id_user) {
        this.id_user = id_user;
    }

    public Integer getMoodleId() {
        return moodleId;
    }

    public void setMoodleId(Integer moodleId) {
        this.moodleId = moodleId;
    }

    public @NotNull String getUsername() {
        return username;
    }

    public void setUsername(@NotNull String username) {
        this.username = username;
    }

    public @NotNull String getFirstname() {
        return firstname;
    }

    public void setFirstname(@NotNull String firstname) {
        this.firstname = firstname;
    }

    public @NotNull String getLastname() {
        return lastname;
    }

    public void setLastname(@NotNull String lastname) {
        this.lastname = lastname;
    }

    public @NotNull String getPassword() {
        return password;
    }

    public void setPassword(@NotNull String password) {
        this.password = password;
    }

    public @NotNull String getEmail() {
        return email;
    }

    public void setEmail(@NotNull String email) {
        this.email = email;
    }
}
