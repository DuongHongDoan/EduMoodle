package com.example.edumoodle.Controller.student;

import com.example.edumoodle.DTO.CoursesDTO;
import com.example.edumoodle.Service.MyCoursesStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;

@Controller
public class StudentCoursesController {

    @Autowired
    private MyCoursesStudentService myCoursesStudentService;

    @GetMapping("/user/my-student-courses") // Ensure this matches the URL you are trying to access
    public String showStudentCourses(Model model) {
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
        List<CoursesDTO> userCourses = myCoursesStudentService.getUserCourses(username);
        model.addAttribute("userCourses", userCourses);
        System.out.println("User Courses: " + userCourses);

        // Add any necessary attributes to the model
        return "student/mystudent_courses"; // Make sure this matches the template name
    }

    @PostMapping("/my-courses-student")
    public String processCourseSearch(@RequestParam("username") String username,
                                      @RequestParam(value = "searchQuery", required = false) String searchQuery,
                                      @RequestParam(value = "sort", required = false) String sort,
                                      Model model) {
        if (username == null) {
            model.addAttribute("error", "Không thể xác định được người dùng hiện tại.");
            return "student/mystudent_courses";
        }

        // Get user courses as CoursesDto
        List<CoursesDTO> userCourses = myCoursesStudentService.getUserCourses(username);

        // Filter by search query if provided
        if (searchQuery != null && !searchQuery.isEmpty()) {
            userCourses = myCoursesStudentService.filterCoursesByName(userCourses, searchQuery);
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

        return "student/mystudent_courses";
    }

    // Lấy chi tiết một khóa học dựa trên moodleCourseId
    @GetMapping("/user/student_course_details")
    public String getCourseDetails(@RequestParam("moodleCourseId") Integer moodleCourseId, Model model) {
        CoursesDTO courseDetails = myCoursesStudentService.getCourseDetails(moodleCourseId);

        // Kiểm tra xem có đúng không
        System.out.println("Course Details: " + courseDetails);

        // Gửi dữ liệu đến view
        model.addAttribute("courseDetails", courseDetails);

        return "student/mystudent_course_details";
    }

    @GetMapping("/user/fetch_module_content")
    public String fetchModuleContent(@RequestParam("moodleCourseId") Integer moodleCourseId,
                                     @RequestParam("moduleId") Integer moduleId, Model model) {
        // In ra giá trị moodleCourseId và moduleId trong console
        System.out.println("Moodle Course ID: " + moodleCourseId);
        System.out.println("Module ID: " + moduleId);

        // Thêm các giá trị vào model để chuyển sang view
        model.addAttribute("moodleCourseId", moodleCourseId);
        model.addAttribute("moduleId", moduleId);

        // Trả về view cho nội dung module
        return "student/module_details"; // Đổi tên này thành tên view bạn muốn sử dụng
    }


    // Phương thức xử lý POST khi nhấn vào module để lấy nội dung
    @PostMapping("/user/fetch_module_content")
    public String fetchModuleContent(@RequestParam("moduleId") Integer moduleId,
                                     @RequestParam("moduleType") String moduleType,
                                     @RequestParam("moodleCourseId") Integer moodleCourseId,
                                     Model model) {
        // Ghi lại các tham số nhận được
        System.out.println("Module ID (POST): " + moduleId);
        System.out.println("Module Type (POST): " + moduleType);
        System.out.println("Moodle Course ID (POST): " + moodleCourseId);

        // Lấy nội dung mô-đun bằng cách sử dụng service
        String moduleContent = myCoursesStudentService.fetchModuleContent(moduleId, moduleType, moodleCourseId);

        // Thêm các thuộc tính vào model để hiển thị trong view
        model.addAttribute("moduleId", moduleId);
        model.addAttribute("moodleCourseId", moodleCourseId);
        model.addAttribute("moduleContent", moduleContent); // Thêm nội dung mô-đun ở đây

        return "student/module_details"; // Trả về tên view
    }

}
