package com.example.edumoodle.Controller;

import com.example.edumoodle.DTO.UsersDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/login")
    public String getLogin(Model model, UsersDTO usersDTO) {
        model.addAttribute("user", usersDTO);
        return "common/Login";
    }

}
