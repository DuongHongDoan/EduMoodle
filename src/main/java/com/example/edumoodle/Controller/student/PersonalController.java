package com.example.edumoodle.Controller.student;

import com.example.edumoodle.DTO.RecentlyAccessedCourseDTO;
import com.example.edumoodle.DTO.UsersDTO;
import com.example.edumoodle.Service.CoursesService;
import com.example.edumoodle.Service.MyCoursesStudentService;
import com.example.edumoodle.Service.PersonalService;
import com.example.edumoodle.Service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PersonalController {
    @Autowired
    private PersonalService personalService;
    @Autowired
    private MyCoursesStudentService myCoursesStudentService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private CoursesService coursesService;

    //Trang ca nhan
    @GetMapping("/user/my_recent_courses")
    public String showRecentlyAccessedCoursesPage(Model model) {
        // Get the current user's username from Spring Security
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Retrieve userId based on the username
        UsersDTO usersDTO = usersService.getUserByUsername(username);
        Integer userId = usersDTO.getId();

        // Get the list of recently accessed courses
        List<RecentlyAccessedCourseDTO> recentlyAccessedCourses = personalService.getRecentlyAccessedCourses(userId);

        // Log course IDs for debugging
        recentlyAccessedCourses.forEach(course -> System.out.println("Course ID: " + course.getCourseId()));

        // Lấy tên giảng viên cho từng khóa học đã truy cập gần đây
        recentlyAccessedCourses.forEach(course -> {
            String teacherName = myCoursesStudentService.getTeacherName(course.getCourseId());
            course.setInstructorName(teacherName);  // Gán tên giảng viên vào đối tượng RecentlyAccessedCourseDTO
        });

        // Add the list of courses and username to the model for the view
        model.addAttribute("username", username);
        model.addAttribute("recentlyAccessedCourses", recentlyAccessedCourses);

        // Return the view name
        return "student/student_personal_page1";
    }
}
