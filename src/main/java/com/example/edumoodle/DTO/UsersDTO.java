package com.example.edumoodle.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class UsersDTO {
    private Integer id;

    @NotBlank(message = "Vui lòng nhập username!")
    @Size(min = 4, max = 20, message = "Username phải từ 4 đến 20 ký tự!")
    private String username;

    @NotBlank(message = "Vui lòng nhập Tên đệm và Tên của bạn!")
    private String firstname;

    @NotBlank(message = "Vui lòng nhập Họ của bạn!")
    private String lastname;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Vui lòng nhập email của bạn!")
    private String email;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Size(min = 8, message = "Mật khẩu phải ít nhất 8 ký tự!")
    private String password;

    private String fullname;
    private List<Users_RolesDTO> roles;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Users_RolesDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<Users_RolesDTO> roles) {
        this.roles = roles;
    }
}
