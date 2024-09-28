package com.example.edumoodle.Controller;

import com.example.edumoodle.DTO.CategoriesDTO;
import com.example.edumoodle.DTO.CategoryHierarchyDTO;
import com.example.edumoodle.DTO.CoursesDTO;
import com.example.edumoodle.DTO.UsersDTO;
import com.example.edumoodle.Service.CategoriesService;
import com.example.edumoodle.Service.CoursesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CommonController {

    @Autowired
    private CategoriesService categoriesService;
    @Autowired
    private CoursesService coursesService;

    @GetMapping("/login")
    public String getLogin(Model model, UsersDTO usersDTO) {
        model.addAttribute("user", usersDTO);
        return "common/Login";
    }

    //để tạm
    @GetMapping("/admin/dashboard")
    public String getDashboard() {
        return "admin/Dashboard";
    }

    //home của user (teacher-student) là danh sách categories
    @GetMapping("/user/home")
    public String getHome(Model model) {
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
        return "common/Home";
    }

    //tìm kiếm khóa học bằng ô tìm kiếm
    @GetMapping("/user/courses/search")
    public String searchCourses(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<CoursesDTO> courses;

        if (keyword != null && !keyword.isEmpty()) {
            courses = coursesService.getSearchCourses(keyword);  // Tìm khóa học từ API Moodle
        } else {
            courses = coursesService.getAllCourses();  // Lấy tất cả khóa học nếu không có từ khóa
        }

        model.addAttribute("coursesList", courses);
        model.addAttribute("keyword", keyword);

        Map<Integer, String> categoryMap = coursesService.getMapCategories();
        model.addAttribute("categoryMap", categoryMap);

        List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
        model.addAttribute("categoriesHierarchy", categoriesHierarchy);

        return "common/CoursesList";
    }

    //danh sách toàn bộ các khóa học có trên hệ thống
    @GetMapping("/user/courses")
    public String getCategoriesForSelect(@RequestParam(value = "page", defaultValue = "1") int page,
                                         @RequestParam(value = "size", defaultValue = "3") int size,Model model) {
        List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
        model.addAttribute("categoriesHierarchy", categoriesHierarchy);

        List<CoursesDTO> coursesList = coursesService.getAllCourses();
        model.addAttribute("coursesList", coursesList);

        Map<Integer, String> categoryMap = coursesService.getMapCategories();
        model.addAttribute("categoryMap", categoryMap);

        if (coursesList.size() > size) {
            // Phân trang
            int pageIndex = page - 1;
            int start = pageIndex * size;
            int end = Math.min(start + size, coursesList.size());
            List<CoursesDTO> pagedCourses = coursesList.subList(start, end);
            Page<CoursesDTO> coursePage = new PageImpl<>(pagedCourses, PageRequest.of(pageIndex, size), coursesList.size());
            model.addAttribute("coursePage", coursePage);
        }else {
            model.addAttribute("coursePage", null);
        }
        //đồng bộ dữ liệu course web-moodle
        coursesService.synchronizeCourses(coursesList);

        return "common/CoursesList";
    }

    //lọc danh sách khóa học theo category tương ứng
    @GetMapping("/user/courses/category")
    public String  getCoursesByParentCategory(@RequestParam(value = "categoryId", required = false) Integer categoryId,
                                              @RequestParam(value = "page", defaultValue = "1") int page,
                                              @RequestParam(value = "size", defaultValue = "3") int size, Model model) {
        List<CoursesDTO> coursesOfParent;

        if (categoryId != null) {
            coursesOfParent = coursesService.getCoursesByParentCategory(categoryId);  // Lấy khóa học theo category
        }else {
            coursesOfParent = coursesService.getAllCourses();  // Lấy tất cả khóa học nếu không chọn danh mục
        }

        model.addAttribute("coursesOfParent", coursesOfParent);
        model.addAttribute("categoryId", categoryId);

        Map<Integer, String> categoryMap = coursesService.getMapCategories();
        model.addAttribute("categoryMap", categoryMap);

        List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
        model.addAttribute("categoriesHierarchy", categoriesHierarchy);
        if(coursesOfParent.size() > size) {
            // Phân trang
            int pageIndex = page - 1;
            int start = pageIndex * size;
            int end = Math.min(start + size, coursesOfParent.size());
            List<CoursesDTO> pagedCourses = coursesOfParent.subList(start, end);
            Page<CoursesDTO> coursePage = new PageImpl<>(pagedCourses, PageRequest.of(pageIndex, size), coursesOfParent.size());
            model.addAttribute("coursePage", coursePage);
        } else {
            model.addAttribute("coursePage", null);
        }

        return "common/CoursesList";
    }
}
