package com.example.edumoodle.Controller;

import com.example.edumoodle.DTO.CategoryHierarchyDTO;
import com.example.edumoodle.DTO.CoursesDTO;
import com.example.edumoodle.DTO.SectionsDTO;
import com.example.edumoodle.DTO.UsersDTO;
import com.example.edumoodle.Service.CategoriesService;
import com.example.edumoodle.Service.CoursesService;
import com.example.edumoodle.Service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@Tag(name = "Courses Management", description = "APIs for managing Moodle courses")
public class CoursesController {

    @Autowired
    private CategoriesService categoriesService;

    @Autowired
    private CoursesService coursesService;
    @Autowired
    private UsersService usersService;

    @Operation(summary = "Get all categories for select input", description = "Fetch a list of all categories")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
//    url = /admin/courses
    @GetMapping("/courses")
    public String getCategoriesForSelect(Model model) {
        List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
        model.addAttribute("categoriesHierarchy", categoriesHierarchy);

        List<CoursesDTO> coursesList = coursesService.getAllCourses();
        model.addAttribute("coursesList", coursesList);

        Map<Integer, String> categoryMap = coursesService.getMapCategories();
        model.addAttribute("categoryMap", categoryMap);

        return "admin/ManageCourses";
    }

    @Operation(summary = "hiển thị courses từng category tương ứng", description = "Fetch a list of all courses")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
//   url = /admin/courses/category?categoryId=2
    @GetMapping("/courses/category")
    public String  getCoursesByParentCategory(@RequestParam int categoryId, Model model) {
        List<CoursesDTO> coursesOfParent = coursesService.getCoursesByParentCategory(categoryId);
        model.addAttribute("coursesOfParent", coursesOfParent);

        Map<Integer, String> categoryMap = coursesService.getMapCategories();
        model.addAttribute("categoryMap", categoryMap);

        List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
        model.addAttribute("categoriesHierarchy", categoriesHierarchy);

        model.addAttribute("categoryId", categoryId);

        return "admin/ManageCourses";
    }

    @Operation(summary = "Display course detail", description = "click a course then display course detail")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved course detail")
//    url = /admin/courses/view?courseId
    @GetMapping("/courses/view")
    public String getCourseDetail(@RequestParam int courseId, Model model) {
        List<SectionsDTO> sections = coursesService.getCourseContent(courseId);
        model.addAttribute("sections", sections);

        CoursesDTO courseDetails = coursesService.getCourseDetail(courseId);
        model.addAttribute("courseDetails", courseDetails);
        List<UsersDTO> enrolledUsers = usersService.getEnrolledUsers(courseId);
        model.addAttribute("enrolledUsers", enrolledUsers);
        long studentCount = enrolledUsers.stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> "student".equals(role.getShortname())))
                .count();
        model.addAttribute("studentCount", studentCount);

        List<UsersDTO> usersList = usersService.getAllUsers();
        List<UsersDTO> usersListFilter = usersList.stream()
                .filter(user -> user.getId() != 1 && user.getId() != 2)
                .toList();
        model.addAttribute("usersList", usersListFilter);

        return "admin/DetailCourse";
    }

}
