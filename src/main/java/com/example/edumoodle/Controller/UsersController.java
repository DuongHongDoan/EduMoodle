package com.example.edumoodle.Controller;

import com.example.edumoodle.DTO.CategoriesDTO;
import com.example.edumoodle.DTO.CoursesDTO;
import com.example.edumoodle.DTO.NguoiDungDTO;
import com.example.edumoodle.DTO.UsersDTO;
import com.example.edumoodle.Model.UsersEntity;
import com.example.edumoodle.Service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

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
        //lưu tất cả users vào csdl web
        usersService.saveUsers(usersList);

        return "admin/ManageUsers";
    }

    @Operation(summary = "Handle form create user", description = "handle form create user")
    @ApiResponse(responseCode = "200", description = "Successfully created user")
    //    url = /admin/users/create
    @PostMapping("users/create")
    public String createUser(@Valid @ModelAttribute("usersDTO") UsersDTO usersDTO, BindingResult result, Model model) {
        if (result.hasErrors()) {
            // Nếu có lỗi, thêm đối tượng usersDTO vào model, Trả về form nếu có lỗi
            model.addAttribute("usersDTO", usersDTO);
            return "admin/AddNewUser";
        }
        // Nếu không có lỗi, gọi service để tạo người dùng mới
        usersService.createNewUser(usersDTO);
        return "redirect:/admin/users";
    }

    //    url = /admin/users/add-user --> trả về view form tạo người dùng mới
    @GetMapping("/users/add-user")
    public String getFormAddNewUser(Model model) {
        model.addAttribute("usersDTO", new UsersDTO());
        return "admin/AddNewUser";
    }

    @Operation(summary = "Handle form edit user", description = "handle form edit user")
    @ApiResponse(responseCode = "200", description = "Successfully edited user")
    //    url = /admin/users/edit
    @PostMapping("/users/edit")
    public String editUser(@ModelAttribute("user") NguoiDungDTO usersDTO, BindingResult bindingResult, Model model) {
        boolean success = usersService.editUser(usersDTO);
        if (success) {
            return "redirect:/admin/users";
        } else {
            return "redirect:/admin/users/edit-user?userid=" + usersDTO.getId();
        }
    }

    //    url = /admin/users/edit-user?userid= --> trả về view form sửa người dùng tương ứng
    @GetMapping("/users/edit-user")
    public String getFormAddNewUserForEdit(@RequestParam("userid") Integer userid, Model model) {
        UsersDTO user = usersService.getUserByID(userid);
        model.addAttribute("user", user);
        return "admin/EditUser";
    }

    @Operation(summary = "Delete user by id", description = "Delete user by id")
    @ApiResponse(responseCode = "200", description = "Successfully deleted user")
    //    url = /admin/users/delete
    @GetMapping("/users/delete")
    public String deleteUser(@RequestParam("userid") Integer userId, RedirectAttributes redirectAttributes) {
        try {
            usersService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Người dùng đã được xóa thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa người dùng.");
        }
        return "redirect:/admin/users";  // Điều hướng lại trang danh sách người dùng
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
