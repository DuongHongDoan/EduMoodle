package com.example.edumoodle.Controller;

import com.example.edumoodle.DTO.CoursesDTO;
import com.example.edumoodle.DTO.UsersDTO;
import com.example.edumoodle.Service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String getAllUsers(@RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "size", defaultValue = "50") int size, Model model) {
        List<UsersDTO> usersList = usersService.getAllUsers();
        List<UsersDTO> usersListFilter = usersList.stream()
                .filter(user -> user.getId() != 1 && user.getId() != 2)
                .toList();
        int userCount =usersListFilter.size();
        model.addAttribute("usersList", usersListFilter);
        model.addAttribute("userCount", userCount);

        if (userCount >= size) {
            // Phân trang
            int pageIndex = page - 1;
            int start = pageIndex * size;
            int end = Math.min(start + size, userCount);
            List<UsersDTO> pagedCourses = usersListFilter.subList(start, end);
            Page<UsersDTO> coursePage = new PageImpl<>(pagedCourses, PageRequest.of(pageIndex, size), userCount);
            model.addAttribute("coursePage", coursePage);
        }else {
            model.addAttribute("coursePage", null);
        }

        return "admin/ManageUsers";
    }

    @Operation(summary = "Display search user", description = "enter keyword in search input to search user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user list for search")
//    url = /admin/users/search
    @GetMapping("users/search")
    public String searchUsers(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<UsersDTO> users;

        if(keyword != null && !keyword.isEmpty()) {
            users = usersService.getSearchUser(keyword);
        } else {
            users = List.of();
        }

        List<UsersDTO> usersListFilter = users.stream()
                .filter(user -> user.getId() != 1 && user.getId() != 2)
                .toList();
        int userCount =usersListFilter.size();
        model.addAttribute("userCount", userCount);
        model.addAttribute("usersList", usersListFilter);
        model.addAttribute("keyword", keyword);

        return "admin/ManageUsers";
    }
}
