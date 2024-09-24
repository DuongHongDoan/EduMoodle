package com.example.edumoodle.Controller;

import com.example.edumoodle.DTO.CategoriesDTO;
import com.example.edumoodle.DTO.CoursesDTO;
import com.example.edumoodle.DTO.NguoiDungDTO;
import com.example.edumoodle.DTO.UsersDTO;
import com.example.edumoodle.Model.UsersEntity;
import com.example.edumoodle.Repository.UsersRepository;
import com.example.edumoodle.Service.UserInterface;
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

    @Autowired
    private UserInterface userInterface;

    @Autowired
    private UsersRepository usersRepository;

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
//        usersService.saveUsers(usersList);

        return "admin/ManageUsers";
    }

    @Operation(summary = "Handle form create user", description = "handle form create user")
    @ApiResponse(responseCode = "200", description = "Successfully created user")
    //    url = /admin/users/create
    @PostMapping("users/create")
    public String createUser(@Valid @ModelAttribute("usersDTO") UsersDTO usersDTO, BindingResult result,
                             RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            // Nếu có lỗi, thêm đối tượng usersDTO vào model, Trả về form nếu có lỗi
            model.addAttribute("usersDTO", usersDTO);
            return "admin/AddNewUser";
        }

        // Gọi service để thực hiện các bước kiểm tra
        String validationError = usersService.validateCreateUser(usersDTO);
        if (validationError != null) {
            model.addAttribute("errorMessage", validationError);
            return "admin/AddNewUser";
        }

        // Lưu thông tin người dùng vào cơ sở dữ liệu của ứng dụng web
        UsersEntity savedUser = userInterface.save(usersDTO);

        // Nếu không có lỗi, gọi service để tạo người dùng mới trên moodle
        String moodleUserId = usersService.createNewUser(usersDTO);

        // Kiểm tra phản hồi từ Moodle để check moodleUserId trong csdl web
        if (moodleUserId != null) {
            savedUser.setMoodleId(Integer.parseInt(moodleUserId)); // Chuyển đổi moodleUserId từ String sang int
            userInterface.update(savedUser);

            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công.");
            return "redirect:/admin/users";
        } else {
            // Xóa người dùng khỏi cơ sở dữ liệu nếu không thể tạo trên Moodle
            userInterface.delete(savedUser.getId_user());
            model.addAttribute("errorMessage", "Đăng ký không thành công. Vui lòng thử lại!");
            return "admin/AddNewUser";
        }
    }

    //    url = /admin/users/add-user --> trả về view form tạo người dùng mới
    @GetMapping("/users/add-user")
    public String getFormAddNewUser(Model model) {
        model.addAttribute("usersDTO", new UsersDTO());
        return "admin/AddNewUser";
    }

//    @Operation(summary = "Handle form edit user", description = "handle form edit user")
//    @ApiResponse(responseCode = "200", description = "Successfully edited user")
//    //    url = /admin/users/edit
//    @PostMapping("/users/edit")
//    public String editUser(@ModelAttribute("user") NguoiDungDTO usersDTO, BindingResult bindingResult,
//                           RedirectAttributes redirectAttributes, Model model) {
//        // Tìm người dùng theo moodle_id
//        Optional<UsersEntity> userOptional = usersRepository.findByMoodleId(usersDTO.getId());
//
//        if (userOptional.isEmpty()) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Người dùng không tồn tại.");
//            return "redirect:/admin/users"; // Nếu không tìm thấy, chuyển hướng về danh sách người dùng
//        }
//
//        UsersEntity existingUser = userOptional.get();
//
//        // Cập nhật thông tin cho người dùng từ DTO
//        existingUser.setUsername(usersDTO.getUsername());
//        existingUser.setFirstname(usersDTO.getFirstname());
//        existingUser.setLastname(usersDTO.getLastname());
//        existingUser.setEmail(usersDTO.getEmail());
//
//        // Nếu có cần thay đổi mật khẩu, thêm vào đây
//        if (usersDTO.getPassword() != null && !usersDTO.getPassword().isEmpty()) {
//            existingUser.setPassword(usersDTO.getPassword()); // Chỉ thay đổi mật khẩu nếu có mật khẩu mới
//        }
//
//        // Lưu cập nhật vào cơ sở dữ liệu
//        // Chuyển đổi UsersEntity sang NguoiDungDTO trước khi gọi saveEdit
//        NguoiDungDTO updatedUserDTO = new NguoiDungDTO();
//        updatedUserDTO.setId(existingUser.getMoodleId());
//        updatedUserDTO.setUsername(existingUser.getUsername());
//        updatedUserDTO.setFirstname(existingUser.getFirstname());
//        updatedUserDTO.setLastname(existingUser.getLastname());
//        updatedUserDTO.setEmail(existingUser.getEmail());
//
//        userInterface.saveEdit(updatedUserDTO); // Gọi phương thức saveEdit với DTO
//
//        redirectAttributes.addFlashAttribute("successMessage", "Chỉnh sửa người dùng thành công.");
//        return "redirect:/admin/users";
//    }

    //    url = /admin/users/edit-user?userid= --> trả về view form sửa người dùng tương ứng
    @GetMapping("/users/edit-user")
    public String getFormAddNewUserForEdit(@RequestParam("userid") Integer userid, RedirectAttributes redirectAttributes, Model model) {
//        Optional<UsersEntity> userOption = userInterface.findById(userid);
//        if (userOption.isEmpty()) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Người dùng không tồn tại.");
//            return "redirect:/admin/users";
//        }
//
//        UsersDTO user = usersService.getUserByID(userid);
//        model.addAttribute("user", user);
//        return "admin/EditUser";
        UsersDTO user = usersService.getUserByMoodleID(userid);

        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Người dùng không tồn tại.");
            return "redirect:/admin/users";
        }

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
