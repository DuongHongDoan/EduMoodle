package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.NguoiDungDTO;
import com.example.edumoodle.DTO.UsersDTO;
import com.example.edumoodle.Model.UsersEntity;

import java.util.Optional;

public interface UserInterface {
    UsersEntity findByUsername(String username);
    UsersEntity save(UsersDTO usersDTO);
    UsersEntity saveEdit(NguoiDungDTO usersDTO);
    void delete(int id);
    UsersEntity update(UsersEntity user);
}
