package com.example.edumoodle.Controller.student;

import com.example.edumoodle.DTO.AttemptIDTO;
import com.example.edumoodle.DTO.CoursesDTO;
import com.example.edumoodle.DTO.QuestionDetail;
import com.example.edumoodle.DTO.QuizDTO;
import com.example.edumoodle.Service.MyCoursesStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @PostMapping("/user/my-courses-student")
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
        // Get the current username from Spring Security
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Fetch userId based on the username
        int userId = myCoursesStudentService.getUserIdByUsername(username);

        // Log details for debugging
        System.out.println("Username: " + username);
        System.out.println("UserID (GET): " + userId);
        System.out.println("Moodle Course ID: " + moodleCourseId);

        // Fetch course details based on moodleCourseId
        CoursesDTO courseDetails = myCoursesStudentService.getCourseDetails(moodleCourseId);

        // Log course details
        System.out.println("Course Details: " + courseDetails);

        // Add course details and other attributes to the model
        model.addAttribute("courseDetails", courseDetails);
        model.addAttribute("userId", userId);
        model.addAttribute("moodleCourseId", moodleCourseId);

        return "student/mystudent_course_details"; // Return the view name
    }


    @GetMapping("/user/fetch_module_content")
    public String fetchModuleContent(@RequestParam("moodleCourseId") Integer moodleCourseId,
                                     @RequestParam("moduleId") Integer moduleId,
                                     @RequestParam("userId") Integer userId, // Added userId
                                     @RequestParam("moduleType") String moduleType, // Added moduleType
                                     Model model) {
        // Fetch module content using service method
        Object moduleContent = myCoursesStudentService.fetchModuleContent(moduleId, moduleType, moodleCourseId);

        // Add these values to the model
        model.addAttribute("moodleCourseId", moodleCourseId);
        model.addAttribute("moduleId", moduleId);
        model.addAttribute("userId", userId); // Add userId to the model
        model.addAttribute("moduleType", moduleType); // Add moduleType to the model

        return "student/module_details"; // Return the module details view
    }

    @PostMapping("/user/fetch_module_content")
    public String fetchModuleContentPost(@RequestParam("moodleCourseId") Integer moodleCourseId,
                                         @RequestParam("moduleId") Integer moduleId,
                                         @RequestParam("userId") Integer userId,
                                         @RequestParam("moduleType") String moduleType,
                                         Model model) {
        // Log Moodle Course ID, Module ID, User ID, and Module Type for debugging
        System.out.println("Moodle Course ID (POST): " + moodleCourseId);
        System.out.println("Module ID (POST): " + moduleId);
        System.out.println("User ID (POST): " + userId);
        System.out.println("Module Type (POST): " + moduleType);

        // Fetch module content using service method
        Object moduleContent = myCoursesStudentService.fetchModuleContent(moduleId, moduleType, moodleCourseId);

        // Check if content is a list (for quizzes) or other content
        if (moduleContent instanceof List<?>) {
            // Assuming it's a list of QuizDTO
            List<QuizDTO> quizzes = (List<QuizDTO>) moduleContent;
            model.addAttribute("quizzes", quizzes); // Add quizzes to the model
        } else {
            model.addAttribute("moduleContent", moduleContent);
        }

        // Add these values to the model
        model.addAttribute("moodleCourseId", moodleCourseId);
        model.addAttribute("moduleId", moduleId);
        model.addAttribute("userId", userId);
        model.addAttribute("moduleType", moduleType);

        return "student/module_details"; // Return the module details view
    }

    @GetMapping("/user/quizCourseStudent/{quizId}/{userId}")
    public String showQuizDetailsGet(@PathVariable("quizId") Integer quizId,
                                     @PathVariable("userId") Integer userId, Model model) {
        // Lấy danh sách attempts của sinh viên cho quiz
        List<AttemptIDTO> attempts = myCoursesStudentService.getStudentAttempts(userId.toString(), quizId.toString());

        // Thêm attempts vào model để hiển thị trên giao diện
        model.addAttribute("quizId", quizId);
        model.addAttribute("userId", userId);
        model.addAttribute("quizDetails", attempts); // Đổi từ 'attempts' thành 'quizDetails'

        // Trả về view 'student/quiz_details' để hiển thị chi tiết quiz
        return "student/quiz_details";
    }


    @PostMapping("/user/quizCourseStudent")
    public String showQuizDetailsPost(@RequestParam("quizId") Integer quizId,
                                      @RequestParam("userId") Integer userId, Model model) {
        // In ra để kiểm tra xem quizId và userId có được truyền đúng không
        System.out.println("POST - Quiz ID: " + quizId + ", User ID: " + userId);

        // Lấy danh sách attempts của sinh viên cho quiz
        List<AttemptIDTO> attempts = myCoursesStudentService.getStudentAttempts(userId.toString(), quizId.toString());

        // Thêm attempts vào model để hiển thị trên giao diện
        model.addAttribute("quizId", quizId);
        model.addAttribute("userId", userId);
        model.addAttribute("quizDetails", attempts); // Đảm bảo tên đúng

        // Trả về view 'student/quiz_details' để hiển thị chi tiết quiz
        return "student/quiz_details";
    }


    @GetMapping("/user/quiz/review")
    public String reviewQuiz(@RequestParam("attemptId") int attemptId, Model model) {
        // Gọi service để lấy thông tin chi tiết của quiz dựa trên attemptId
        List<QuestionDetail> questionDetails = myCoursesStudentService.getAttemptDetails(attemptId);

        // Tính số câu đúng và sai
        long correctCount = questionDetails.stream().filter(QuestionDetail::isCorrect).count();
        long totalQuestions = questionDetails.size();
        long incorrectCount = totalQuestions - correctCount;

        // Thêm các thông tin cần thiết vào model để hiển thị trên view
        model.addAttribute("questionDetails", questionDetails);
        model.addAttribute("correctCount", correctCount);
        model.addAttribute("incorrectCount", incorrectCount);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("attemptId", attemptId); // Truyền attemptId qua view nếu cần

        System.out.println("Attempt ID: " + attemptId);

        return "student/quiz_review"; // Trả về view "quiz_review.html"
    }



    // POST mapping để xử lý khi người dùng nhấn vào "Xem lại"
    @PostMapping("/user/quiz/review")
    public String submitReviewQuiz(@RequestParam("attemptId") int attemptId, RedirectAttributes redirectAttributes, Model model) {
        List<QuestionDetail> questionDetails = myCoursesStudentService.getAttemptDetails(attemptId);

        long correctCount = questionDetails.stream().filter(QuestionDetail::isCorrect).count();
        long totalQuestions = questionDetails.size();
        long incorrectCount = totalQuestions - correctCount;

        model.addAttribute("questionDetails", questionDetails);
        model.addAttribute("correctCount", correctCount);
        model.addAttribute("incorrectCount", incorrectCount);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("attemptId", attemptId);

        redirectAttributes.addAttribute("attemptId", attemptId);
        return "student/quiz_review";
    }



}
