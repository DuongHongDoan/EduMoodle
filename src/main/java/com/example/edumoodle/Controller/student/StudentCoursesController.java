package com.example.edumoodle.Controller.student;

import com.example.edumoodle.DTO.CoursesDTO;
import com.example.edumoodle.Repository.CoursesRepository;
import com.example.edumoodle.Service.CategoriesService;
import com.example.edumoodle.Service.MyCoursesStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

}
