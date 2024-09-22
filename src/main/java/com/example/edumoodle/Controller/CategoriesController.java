package com.example.edumoodle.Controller;

import com.example.edumoodle.DTO.CategoriesDTO;
import com.example.edumoodle.DTO.CategoryHierarchyDTO;
import com.example.edumoodle.DTO.CoursesDTO;
import com.example.edumoodle.Service.CategoriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@Tag(name = "Category Management", description = "APIs for managing Moodle categories")
public class CategoriesController {

    @Autowired
    private CategoriesService categoriesService;

    @Operation(summary = "Get all categories", description = "Fetch a list of all categories")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping("/categories")
    public String getAllCategories(Model model) {
        Map<Integer, List<CategoriesDTO>> categories = categoriesService.getCategoriesGroupedByParent();
        if (categories == null || categories.isEmpty()) {
            // Khởi tạo một Map rỗng để tránh lỗi null pointer
            categories = new HashMap<>();
        }

        // Lấy tổng số khóa học cho từng danh mục cha
        Map<Integer, Integer> totalCoursesByParent = categoriesService.getTotalCoursesByParent();
        if (totalCoursesByParent == null) {
            totalCoursesByParent = new HashMap<>();
        }

        List<CategoriesDTO> cateTest = categoriesService.getAllCategory();
        categoriesService.saveCategories(cateTest);
        model.addAttribute("cateTest", cateTest);

        model.addAttribute("categories", categories);
        model.addAttribute("totalCoursesByParent", totalCoursesByParent);
        return "admin/ManageCategory";
    }

    @Operation(summary = "handle form create category", description = "Handle create category")
    @ApiResponse(responseCode = "200", description = "Successfully created category")
    @PostMapping("/categories/create")
    public String createCategory(@ModelAttribute CategoriesDTO categoriesDTO) {
        categoriesService.createCategory(categoriesDTO);
        return "redirect:/admin/categories"; // Chuyển hướng sau khi tạo thành công
    }


    @GetMapping("/categories/create-category")
    public String getCategoriesList(Model model) {
        List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
        model.addAttribute("categoriesHierarchy", categoriesHierarchy);

        return "admin/CreateCategory";
    }
}
