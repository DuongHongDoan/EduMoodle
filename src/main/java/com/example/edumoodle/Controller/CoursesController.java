package com.example.edumoodle.Controller;

import com.example.edumoodle.Service.CoursesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@Tag(name = "Courses Management", description = "APIs for managing Moodle courses")
public class CoursesController {

    @Autowired
    private CoursesService coursesService;

    @Operation(summary = "Get all categories for select input", description = "Fetch a list of all categories")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping("/courses")
    public String getCategoriesForSelect(Model model) {
        List<String> categoriesHierarchy = coursesService.getParentChildCategories();

        // Đưa danh sách phân cấp cha/con vào model để gửi tới giao diện
        model.addAttribute("categoriesHierarchy", categoriesHierarchy);

        return "admin/ManageCourses";
    }
}
