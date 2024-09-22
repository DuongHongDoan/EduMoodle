package com.example.edumoodle.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table (name = "tbl_User")
public class UsersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_user;

    private Integer moodleId;

    @NotNull (message = "Không bỏ trống username")
    private String username;

    @NotNull (message = "Không bỏ trống auth")
    private String auth;

    @NotNull (message = "Không bỏ trống firstname")
    private String firstname;

    @NotNull (message = "Không bỏ trống lastname")
    private String lastname;

    @NotNull (message = "Không bỏ trống password")
    private String password;

    @NotNull (message = "Không bỏ trống email")
    private String email;

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

    public @NotNull String getAuth() {
        return auth;
    }

    public void setAuth(@NotNull String auth) {
        this.auth = auth;
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
