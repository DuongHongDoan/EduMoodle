package com.example.edumoodle.Controller;

import com.example.edumoodle.DTO.*;
import com.example.edumoodle.Model.*;
import com.example.edumoodle.Repository.*;
import com.example.edumoodle.Service.CategoriesService;
import com.example.edumoodle.Service.CoursesService;
import com.example.edumoodle.Service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//controller chung cho vai trò student, teacher và những người dùng không có vai trò
@Controller
public class CommonController {

    @Autowired
    private CategoriesService categoriesService;
    @Autowired
    private CoursesService coursesService;
    @Autowired
    private UsersService usersService;

    @Autowired
    private CoursesRepository coursesRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private RolesRepository rolesRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private SchoolYearSemesterRepository schoolYearSemesterRepository;
    @Autowired
    private CourseGroupsRepository courseGroupsRepository;
    @Autowired
    private CourseAssignmentRepository courseAssignmentRepository;

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

//dành cho sv-gv
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

//dành cho: admin-gv

    // url = /manage/courses/enrol-users --> trả ra form nhập danh sách người dùng đăng ký role vào course
    @GetMapping("/manage/courses/enrol-users")
    public String getFormUploadEnrolUsers() {
        return "common/UploadEnrolUsers";
    }

    @Operation(summary = "Upload user list to enrol course", description = "Handle form upload user list to enrol course")
    @ApiResponse(responseCode = "200", description = "Successfully enrolled user list")
    //url = /admin/users/upload-users
    @PostMapping("/manage/courses/enrol-users")
    public String enrollUserList(@RequestParam("file") MultipartFile file, @RequestParam("type") String type, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("errorMessage", "File không được để trống");
            return "common/UploadEnrolUsers";
        }

        String fileType = file.getContentType();
        if (!"text/csv".equals(fileType)) {
            model.addAttribute("errorMessage", "Chỉ chấp nhận file định dạng CSV");
            return "common/UploadEnrolUsers";
        }

        try {
            // Đọc và xử lý file CSV
            List<EnrolUserDTO> enrolUsers = usersService.parseCSVFileEnrolUsers(file, type);

            for (EnrolUserDTO enrolUser : enrolUsers) {
                usersService.enrolUser(enrolUser);

                for(Integer userId : enrolUser.getUserid()) {
                    UsersEntity user = usersRepository.findByMoodleId(userId).orElse(null);
                    if (user == null) {
                        model.addAttribute("errorMessage", "Người dùng không tồn tại.");
                        return "common/UploadEnrolUsers";
                    }

                    RolesEntity roleName = rolesRepository.findByMoodleId(enrolUser.getRoleid()).orElse(null);
                    assert roleName != null;
                    RolesEntity role = rolesRepository.findByName(roleName.getName());
                    if (role == null) {
                        model.addAttribute("errorMessage", "Vai trò không tồn tại.");
                        return "common/UploadEnrolUsers";
                    }

                    // Tạo quan hệ giữa user và role
                    UserRoleEntity userRole = new UserRoleEntity();
                    userRole.setUsersEntity(user);
                    userRole.setRolesEntity(role);
                    userRoleRepository.save(userRole);

                    //đăng ký người dùng với khóa học tương ứng
                    Optional<CoursesEntity> courseOpt = coursesRepository.findByMoodleId(enrolUser.getCourseid());
                    if(courseOpt.isPresent()) {
                        CoursesEntity course = courseOpt.get();
                        CourseGroupsEntity courseGroup = courseGroupsRepository.findByCoursesEntity(course);

                        CourseAssignmentEntity courseAssignment = new CourseAssignmentEntity();
                        courseAssignment.setUserRoleEntity(userRole);
                        courseAssignment.setCourseGroupsEntity(courseGroup);
                        courseAssignmentRepository.save(courseAssignment);
                    }
                }
            }
            // Nếu thành công, chuyển hướng tới trang danh sách thành viên
            model.addAttribute("successMessage", "Thêm danh sách đăng ký khóa học thành công!");
            return "common/UploadEnrolUsers";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "common/UploadEnrolUsers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "common/UploadEnrolUsers";
        }
    }
}
