package com.example.edumoodle.Controller;

import com.example.edumoodle.DTO.CategoryHierarchyDTO;
import com.example.edumoodle.DTO.CoursesDTO;
import com.example.edumoodle.Service.CategoriesService;
import com.example.edumoodle.Service.CoursesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @Operation(summary = "Get all categories for select input", description = "Fetch a list of all categories")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
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
    @GetMapping("/courses/categoryid/{categoryId}")
    public String  getCoursesByParentCategory(@PathVariable int categoryId, Model model) {
        List<CoursesDTO> coursesOfParent = coursesService.getCoursesByParentCategory(categoryId);
        model.addAttribute("coursesOfParent", coursesOfParent);

        Map<Integer, String> categoryMap = coursesService.getMapCategories();
        model.addAttribute("categoryMap", categoryMap);

        List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
        model.addAttribute("categoriesHierarchy", categoriesHierarchy);

        model.addAttribute("categoryId", categoryId);

        return "admin/ManageCourses";
    }

}
