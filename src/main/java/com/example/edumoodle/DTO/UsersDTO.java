package com.example.edumoodle.DTO;

import java.util.List;

public class UsersDTO {
    private Integer id;
    private String username;
    private String fullname;
    private String email;
    private List<Users_RolesDTO> roles;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Users_RolesDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<Users_RolesDTO> roles) {
        this.roles = roles;
    }
}
