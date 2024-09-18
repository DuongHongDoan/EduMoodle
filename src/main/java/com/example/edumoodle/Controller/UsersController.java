package com.example.edumoodle.Controller;

import com.example.edumoodle.DTO.UsersDTO;
import com.example.edumoodle.Service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@Tag(name = "Users Management", description = "APIs for managing users")
public class UsersController {

    @Autowired
    private UsersService usersService;

    @Operation(summary = "Display users list", description = "display all users")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved users list")
//    url = /admin/users
    @GetMapping("/users")
    public String getAllUsers(Model model) {
        List<UsersDTO> usersList = usersService.getAllUsers();
        List<UsersDTO> usersListFilter = usersList.stream()
                .filter(user -> user.getId() != 1 && user.getId() != 2)
                .toList();
        int userCount =usersListFilter.size();
        model.addAttribute("usersList", usersListFilter);
        model.addAttribute("userCount", userCount);

        return "admin/ManageUsers";
    }
}
