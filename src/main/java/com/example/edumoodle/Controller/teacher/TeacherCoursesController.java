package com.example.edumoodle.Controller.teacher;

import com.example.edumoodle.DTO.*;
import com.example.edumoodle.Model.*;
import com.example.edumoodle.Repository.*;
import com.example.edumoodle.Service.CategoriesService;
//import com.example.edumoodle.Service.QuestionCategoriesService;
import com.example.edumoodle.Service.CoursesService;
import com.example.edumoodle.Service.TeacherCoursesService;
import com.example.edumoodle.Service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/teacher")
@Tag(name = "Courses Management", description = "APIs for managing Moodle courses")
public class TeacherCoursesController {
    @Autowired

    private CategoriesService categoriesService;
    @Autowired

    private TeacherCoursesService TeacherCoursesService;
    @Autowired
    private CoursesService coursesService;
    @Autowired
    private UsersService usersService;

    @Autowired
    private CoursesRepository coursesRepository;
    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private RolesRepository rolesRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;

    @Operation(summary = "Get all categories for select input", description = "Fetch a list of all categories")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    //    url = /teacher/courses
    @GetMapping("/courses")
    public String getCategoriesForSelect(@RequestParam(value = "page", defaultValue = "1") int page,
                                         @RequestParam(value = "size", defaultValue = "3") int size, Model model) {
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
        } else {
            model.addAttribute("coursePage", null);
        }
        //đồng bộ dữ liệu course web-moodle
        coursesService.synchronizeCourses(coursesList);

        return "teacher/ManageCourses";
    }

    @Operation(summary = "hiển thị courses từng category tương ứng", description = "Fetch a list of all courses")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    //   url = /teacher/courses/category?categoryId=2
    @GetMapping("/courses/category")
    public String getCoursesByParentCategory(@RequestParam(value = "categoryId", required = false) Integer categoryId,
                                             @RequestParam(value = "page", defaultValue = "1") int page,
                                             @RequestParam(value = "size", defaultValue = "3") int size, Model model) {
        List<CoursesDTO> coursesOfParent;

        if (categoryId != null) {
            coursesOfParent = coursesService.getCoursesByParentCategory(categoryId);  // Lấy khóa học theo category
        } else {
            coursesOfParent = coursesService.getAllCourses();  // Lấy tất cả khóa học nếu không chọn danh mục
        }

        model.addAttribute("coursesOfParent", coursesOfParent);
        model.addAttribute("categoryId", categoryId);

        Map<Integer, String> categoryMap = coursesService.getMapCategories();
        model.addAttribute("categoryMap", categoryMap);

        List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
        model.addAttribute("categoriesHierarchy", categoriesHierarchy);
        if (coursesOfParent.size() > size) {
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

        return "teacher/ManageCourses";
    }

    @Operation(summary = "Display course detail", description = "click a course then display course detail")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved course detail")
    //    url = /teacher/courses/view?courseId=
    @GetMapping("/courses/view")
    public String getCourseDetail(@RequestParam Integer courseId, Model model) {
        List<SectionsDTO> sections = coursesService.getCourseContent(courseId);
        model.addAttribute("sections", sections);

        CoursesDTO courseDetails = coursesService.getCourseDetail(courseId);
        model.addAttribute("courseDetails", courseDetails);
        List<UsersDTO> enrolledUsers = usersService.getEnrolledUsers(courseId);
        model.addAttribute("enrolledUsers", enrolledUsers);
        long teacherCount = enrolledUsers.stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> "teacher".equals(role.getShortname())))
                .count();
        model.addAttribute("teacherCount", teacherCount);

        List<UsersDTO> usersList = usersService.getAllUsers();
        // Lọc ra những sinh viên chưa đăng ký khóa học
        List<UsersDTO> usersListNotEnrolled = usersList.stream()
                .filter(user -> user.getId() != 1 && user.getId() != 2) // Loại bỏ các user ID không liên quan
                .filter(user -> enrolledUsers.stream().noneMatch(enrolledUser -> enrolledUser.getId().equals(user.getId())))
                .toList();
        model.addAttribute("usersList", usersListNotEnrolled);
        model.addAttribute("course", courseId);

        return "teacher/DetailCourse";
    }

    @Operation(summary = "Enrol users to course", description = "Enrol users to course")
    @ApiResponse(responseCode = "200", description = "Successfully enrolled user")
    //    url = /teacher/courses/enrolUser
    @PostMapping("/courses/enrolUser")
    public String enrolUser(@RequestParam("userIds") List<Integer> userIds,
                            @RequestParam("roleId") Integer roleId,
                            @RequestParam("courseId") Integer courseId, RedirectAttributes redirectAttributes) {
        // Khởi tạo đối tượng DTO và gọi service để đăng ký người dun vào khóa học với role tương ứng
        EnrolUserDTO enrolUserDTO = new EnrolUserDTO();
        enrolUserDTO.setUserid(userIds);
        enrolUserDTO.setRoleid(roleId);
        enrolUserDTO.setCourseid(courseId);
        usersService.enrolUser(enrolUserDTO);

        //đăng ký vai trò teacher-teacher cho việc xác định role để đăng nhập
        for (Integer userId : userIds) {
            UsersEntity user = usersRepository.findByMoodleId(userId).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Người dùng không tồn tại.");
                return "redirect:/teacher/users/manage-role";
            }

            RolesEntity roleName = rolesRepository.findByMoodleId(roleId).orElse(null);
            assert roleName != null;
            RolesEntity role = rolesRepository.findByName(roleName.getName());
            if (role == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vai trò không tồn tại.");
                return "redirect:/teacher/users/manage-role";
            }

            // Tạo quan hệ giữa user và role
            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setUsersEntity(user);
            userRole.setRolesEntity(role);
            userRoleRepository.save(userRole);
        }

        return "redirect:/teacher/courses/view?courseId=" + enrolUserDTO.getCourseid();
    }

    @Operation(summary = "Unenrol user from course", description = "Unenrol user from course")
    @ApiResponse(responseCode = "200", description = "Successfully unenrolled user")
    //    url = /teacher/courses/unenrol
    @GetMapping("/courses/unenrol")
    public String unEnrolUser(@RequestParam("userid") Integer userid,
                              @RequestParam("courseid") Integer courseid,
                              RedirectAttributes redirectAttributes) {
        try {
            usersService.unEnrolUser(userid, courseid);
            redirectAttributes.addFlashAttribute("successMessage", "Người dùng đã bị xóa khỏi khóa học.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa người dùng khỏi khóa học.");
        }
        return "redirect:/teacher/courses/view?courseId=" + courseid;
    }

    @Operation(summary = "Display search course", description = "enter keyword in search input to search course")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved course list for search")
    //    url = /teacher/courses/search
    @GetMapping("/courses/search")
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

        return "teacher/ManageCourses";
    }

    //url = /teacher/courses/create-course
    @GetMapping("/courses/create-course")
    public String showFormCreateCourse(Model model) {
        List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
        model.addAttribute("categoriesHierarchy", categoriesHierarchy);

        CoursesDTO coursesDTO = new CoursesDTO();
        model.addAttribute("coursesDTO", coursesDTO);

        return "teacher/CreateCourse";
    }

    @Operation(summary = "Create course", description = "create course")
    @ApiResponse(responseCode = "200", description = "Successfully created course")
    //    url = /teacher/courses/create-course
    @PostMapping("/courses/create-course")
    public String createCourse(@Valid @ModelAttribute CoursesDTO coursesDTO, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
            model.addAttribute("categoriesHierarchy", categoriesHierarchy);
            model.addAttribute("coursesDTO", coursesDTO);
            return "teacher/CreateCourse";
        }

        // Tạo khóa học trên Moodle
        String moodleResponse = coursesService.createCourse(coursesDTO);

        if (moodleResponse != null && moodleResponse.contains("id")) {
            Integer moodleCourseId = coursesService.extractMoodleCourseId(moodleResponse);
            CategoriesEntity category = categoriesRepository.findByMoodleId(coursesDTO.getCategoryid());
            if (category == null) {
                // Xử lý lỗi nếu category không tồn tại
                model.addAttribute("error", "Category không tồn tại.");
                return "teacher/CreateCourse";
            }

            CoursesEntity newCourse = new CoursesEntity();
            newCourse.setMoodleId(moodleCourseId);
            newCourse.setFullname(coursesDTO.getFullname());
            newCourse.setShortname(coursesDTO.getShortname());
            newCourse.setSummary(coursesDTO.getSummary());
            newCourse.setCategoriesEntity(category);
            coursesRepository.save(newCourse);

            redirectAttributes.addFlashAttribute("successMessage", "Tạo khóa học thành công!");
            return "redirect:/teacher/courses";
        } else {
            // Nếu tạo khóa học thất bại, trả về thông báo lỗi
            model.addAttribute("errorMessage", "Failed to create the course on Moodle. Please try again.");
            List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
            model.addAttribute("categoriesHierarchy", categoriesHierarchy);
            model.addAttribute("coursesDTO", coursesDTO);
            return "teacher/CreateCourse";
        }
    }

    //    url = /teacher/courses/edit-course?courseId= --> trả về view form sửa khóa học tương ứng
    @GetMapping("/courses/edit-course")
    public String showFormEditCourse(@RequestParam("courseId") Integer courseId, RedirectAttributes redirectAttributes, Model model) {
        Optional<CoursesEntity> courseOpt = coursesRepository.findByMoodleId(courseId);
        if (courseOpt.isPresent()) {
            CoursesEntity course = courseOpt.get();
            CoursesDTO coursesDto = new CoursesDTO();
            coursesDto.setId(course.getMoodleId());
            coursesDto.setFullname(course.getFullname());
            coursesDto.setShortname(course.getShortname());
            coursesDto.setCategoryid(course.getCategoriesEntity().getMoodleId());
            coursesDto.setSummary(course.getSummary());

            model.addAttribute("coursesDto", coursesDto);

            List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
            model.addAttribute("categoriesHierarchy", categoriesHierarchy);
            model.addAttribute("courseIdWeb", course.getId_courses());

        } else {
            model.addAttribute("errorMessage", "Course not found.");
        }
        return "teacher/EditCourse";
    }

    @Operation(summary = "Edit course", description = "edit course")
    @ApiResponse(responseCode = "200", description = "Successfully edited course")
    //    url = /teacher/courses/edit-course
    @PostMapping("/courses/edit-course")
    public String editCourse(@Valid @ModelAttribute CoursesDTO coursesDto, @RequestParam("courseIdWeb") Integer courseIdWeb,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
            model.addAttribute("categoriesHierarchy", categoriesHierarchy);
            model.addAttribute("coursesDto", coursesDto);
            model.addAttribute("courseIdWeb", courseIdWeb);
            return "teacher/EditCourse";
        }

        Optional<CoursesEntity> courseOpt = coursesRepository.findByMoodleId(coursesDto.getId());
        CategoriesEntity category = categoriesRepository.findByMoodleId(coursesDto.getCategoryid());
        if (courseOpt.isPresent()) {
            CoursesEntity course = courseOpt.get();
            course.setMoodleId(coursesDto.getId());
            course.setFullname(coursesDto.getFullname());
            course.setShortname(coursesDto.getShortname());
            course.setCategoriesEntity(category); // Sử dụng Integer trực tiếp
            course.setSummary(coursesDto.getSummary());
            coursesRepository.save(course);

            boolean moodleUpdated = coursesService.updateMoodleCourse(coursesDto);
            if (moodleUpdated) {
                redirectAttributes.addFlashAttribute("successMessage", "Sửa khóa học thành công!");
                return "redirect:/teacher/courses";
            } else {
                model.addAttribute("errorMessage", "Cập nhật không thành công. Vui lòng thử lại!");
                List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
                model.addAttribute("categoriesHierarchy", categoriesHierarchy);
                model.addAttribute("coursesDto", coursesDto);
                model.addAttribute("courseIdWeb", courseIdWeb);
                return "teacher/EditCourse";
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Course not found.");
            return "redirect:/teacher/courses";
        }
    }

    @Operation(summary = "Delete course", description = "delete course")
    @ApiResponse(responseCode = "200", description = "Successfully deleted course")
    //    url = /teacher/courses/delete
    @GetMapping("/courses/delete")
    public String deleteMoodleCourse(@RequestParam("courseId") int courseId, RedirectAttributes redirectAttributes) {
        boolean moodleSuccess = coursesService.deleteCourseFromMoodle(courseId);
        if (moodleSuccess) {
            boolean dbSuccess = coursesService.deleteCourseFromDatabase(courseId);
            if (dbSuccess) {
                redirectAttributes.addFlashAttribute("message", "Course deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to delete course from the database.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to delete course from Moodle.");
        }
        return "redirect:/teacher/courses";
    }
    @GetMapping("/user/my-teacher-courses") // Ensure this matches the URL you are trying to access
    public String showteacherCourses(Model model) {
        // Get the current user's information
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;

        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                username = userDetails.getUsername();
            } else {
                username = authentication.getPrincipal().toString();
            }
        }

        // Ensure username is not null
        if (username == null) {
            model.addAttribute("error", "Không thể xác định được người dùng hiện tại.");
            return "my-courses"; // Return view with error message
        }

        // Set username in the model
        model.addAttribute("username", username);

        // Call the service to get user courses
        List<CoursesDTO> userCourses = TeacherCoursesService.getUserCourses(username);
        model.addAttribute("userCourses", userCourses);
        System.out.println("User Courses: " + userCourses);

        // Add any necessary attributes to the model
        return "teacher/mys_courses"; // Make sure this matches the template name
    }

    @PostMapping("/teacher/my-courses-teacher")
    public String processCourseSearch(@RequestParam("username") String username,
                                      @RequestParam(value = "searchQuery", required = false) String searchQuery,
                                      @RequestParam(value = "sort", required = false) String sort,
                                      @RequestParam(value = "showAll", required = false) String showAll,
                                      Model model) {
        if (username == null) {
            model.addAttribute("error", "Không thể xác định được người dùng hiện tại.");
            return "teacher/myteacher_courses";
        }

        // Get all user courses
        List<CoursesDTO> userCourses = TeacherCoursesService.getUserCourses(username);

        // Handle "showAll" button click
        if (showAll == null) {
            // Filter by search query if provided and not showing all
            if (searchQuery != null && !searchQuery.isEmpty()) {
                userCourses = TeacherCoursesService.filterCoursesByName(userCourses, searchQuery);
                if (userCourses.isEmpty()) {
                    model.addAttribute("error", "Không tìm thấy khóa học phù hợp.");
                }
            }
        }

        // Sort if requested
        if ("name".equals(sort)) {
            userCourses.sort(Comparator.comparing(CoursesDTO::getFullname, String.CASE_INSENSITIVE_ORDER));
        }

        // Print out the filtered and/or sorted courses (with Moodle course IDs)
        for (CoursesDTO course : userCourses) {
            System.out.println("Course Moodle ID: " + course.getMoodleCourseId() + ", Course Name: " + course.getFullname());
        }

        // Update model
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("userCourses", userCourses);

        return "teacher/myteacher_courses";
    }



    // Lấy chi tiết một khóa học dựa trên moodleCourseId
    @GetMapping("/user/teacher_course_details")
    public String getCourseDetails(@RequestParam("moodleCourseId") Integer moodleCourseId, Model model) {
        // Get the current username from Spring Security
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Fetch userId based on the username
        int userId = TeacherCoursesService.getUserIdByUsername(username);

        // Log details for debugging
        System.out.println("Username: " + username);
        System.out.println("UserID (GET): " + userId);
        System.out.println("Moodle Course ID: " + moodleCourseId);

        // Fetch course details based on moodleCourseId
//        CoursesDTO courseDetails = TeacherCoursesService.getCourseDetails(moodleCourseId);

        // Log course details
//        System.out.println("Course Details: " + courseDetails);
//
//        // Add course details and other attributes to the model
//        model.addAttribute("courseDetails", courseDetails);
//        model.addAttribute("userId", userId);
//        model.addAttribute("moodleCourseId", moodleCourseId);

        return "teacher/myteacher_course_details"; // Return the view name
    }
}


