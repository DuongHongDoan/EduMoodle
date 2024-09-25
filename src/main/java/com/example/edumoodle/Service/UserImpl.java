package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.NguoiDungDTO;
import com.example.edumoodle.DTO.UsersDTO;
import com.example.edumoodle.Model.UsersEntity;
import com.example.edumoodle.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserImpl implements UserInterface{
    @Autowired
    PasswordEncoder passwordEncoder;

    private UsersRepository userRepo;

    public UserImpl(UsersRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UsersEntity findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    @Override
    public UsersEntity save(UsersDTO userDto) {
        UsersEntity user = new UsersEntity(userDto.getId(), userDto.getUsername(), userDto.getFirstname(), userDto.getLastname(), passwordEncoder.encode(userDto.getPassword()), userDto.getEmail());
        return userRepo.save(user);
    }

    @Override
    public UsersEntity saveEdit(NguoiDungDTO userDto) {
        UsersEntity user = new UsersEntity(userDto.getId(), userDto.getUsername(), userDto.getFirstname(), userDto.getLastname(), passwordEncoder.encode(userDto.getPassword()), userDto.getEmail());
        return userRepo.save(user);
    }

    @Override
    public void delete(int id) {
        userRepo.deleteById(id); // Xóa người dùng theo ID
    }

    @Override
    public UsersEntity update(UsersEntity user) {
        // Có thể mã hóa lại mật khẩu nếu cần thiết
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepo.save(user); // Cập nhật thông tin người dùng
    }
}
