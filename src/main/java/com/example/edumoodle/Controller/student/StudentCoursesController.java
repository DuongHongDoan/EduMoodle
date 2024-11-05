package com.example.edumoodle.Controller.student;

import com.example.edumoodle.DTO.*;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
                                      @RequestParam(value = "showAll", required = false) String showAll,
                                      Model model) {
        if (username == null) {
            model.addAttribute("error", "Không thể xác định được người dùng hiện tại.");
            return "student/mystudent_courses";
        }

        // Get all user courses
        List<CoursesDTO> userCourses = myCoursesStudentService.getUserCourses(username);

        // Handle "showAll" button click
        if (showAll == null) {
            // Filter by search query if provided and not showing all
            if (searchQuery != null && !searchQuery.isEmpty()) {
                userCourses = myCoursesStudentService.filterCoursesByName(userCourses, searchQuery);
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

    // danh sach sinh vien trong khoa hoc
    @GetMapping("/user/student_list")
    public String getStudentList(@RequestParam("moodleCourseId") Integer moodleCourseId,
                                 @RequestParam(value = "selectedLetter", required = false, defaultValue = "") String selectedLetter,
                                 @RequestParam(value = "filterType", required = false, defaultValue = "firstName") String filterType,
                                 Model model,
                                 @RequestParam(value = "isFragment", required = false, defaultValue = "false") boolean isFragment) {
        // Fetch the list of students
        List<StudentsCourseDTO> students = myCoursesStudentService.getEnrolledStudents(moodleCourseId);

        // Trim and validate selectedLetter, and assign it to a new final variable
        final String filteredLetter = selectedLetter.trim().toUpperCase();

        // Filter students by the selected letter and the filter type (firstName or lastName)
        List<StudentsCourseDTO> filteredStudents = students; // Keep original list unchanged
        if (!filteredLetter.isEmpty()) {
            if ("firstName".equals(filterType)) {
                filteredStudents = students.stream()
                        .filter(student -> student.getFirstName() != null && student.getFirstName().toUpperCase().startsWith(filteredLetter))
                        .collect(Collectors.toList());
            } else if ("lastName".equals(filterType)) {
                filteredStudents = students.stream()
                        .filter(student -> student.getLastName() != null && student.getLastName().toUpperCase().startsWith(filteredLetter))
                        .collect(Collectors.toList());
            }
        }

        // Add data to the model
        model.addAttribute("students", filteredStudents);
        model.addAttribute("moodleCourseId", moodleCourseId);
        model.addAttribute("selectedLetter", filteredLetter);
        model.addAttribute("filterType", filterType);

        // Nếu yêu cầu là từ student_course_details, chỉ trả về phần body
        if (isFragment) {
            return "student/course_students_list :: studentListContent"; // Chỉ lấy phần studentListContent
        }

        return "student/course_students_list"; // Trả về toàn bộ view nếu không phải từ student_course_details
    }

    @GetMapping("/user/student_grades")
    public String getStudentGrades(@RequestParam("moodleCourseId") Integer moodleCourseId,
                                   @RequestParam("userId") Integer userId,
                                   Model model) {
        // Thêm moodleCourseId và userId vào model
        model.addAttribute("moodleCourseId", moodleCourseId);
        model.addAttribute("userId", userId);

        // Lấy danh sách điểm của sinh viên
        GradesDTO gradesDTO =  myCoursesStudentService.getStudentGrades(moodleCourseId, userId);

        // Khởi tạo danh sách nếu nó là null
        if (gradesDTO.getGradeItems() == null) {
            gradesDTO.setGradeItems(new ArrayList<GradeItemDTO>());
        }

        model.addAttribute("grades", gradesDTO);
        return "student/course_grades_list";
    }



    @GetMapping("/user/fetch_module_content")
    public String fetchModuleContent(@RequestParam("moodleCourseId") Integer moodleCourseId,
                                     @RequestParam("moduleId") Integer moduleId,
                                     @RequestParam("userId") Integer userId,
                                     @RequestParam("moduleType") String moduleType,
                                     @RequestParam(value = "instanceId", required = false) Integer instanceId, // Thêm instanceId (optional)
                                     Model model) {
        // In ra thông tin để kiểm tra
        System.out.println("Moodle Course ID (GET): " + moodleCourseId);
        System.out.println("Module ID (GET): " + moduleId);
        System.out.println("User ID (GET): " + userId);
        System.out.println("Module Type (GET): " + moduleType);
        if (moduleType.equals("forum") && instanceId != null) {
            System.out.println("Forum Instance ID (GET): " + instanceId);
        }

        // Fetch module content bằng service, có thể cần điều chỉnh nếu là forum và cần thêm instanceId
        Object moduleContent;
        if ("forum".equals(moduleType) && instanceId != null) {
            moduleContent = myCoursesStudentService.fetchForumContent(instanceId);
        } else {
            moduleContent = myCoursesStudentService.fetchModuleContent(moduleId, moduleType, moodleCourseId);
        }

        // Nếu content là List, giả sử là các quiz
        if (moduleContent instanceof List<?>) {
            List<QuizDTO> quizzes = (List<QuizDTO>) moduleContent;
            model.addAttribute("quizzes", quizzes); // Add vào model
        } else {
            model.addAttribute("moduleContent", moduleContent);
        }

        // Add các giá trị vào model
        model.addAttribute("moodleCourseId", moodleCourseId);
        model.addAttribute("moduleId", moduleId);
        model.addAttribute("userId", userId);
        model.addAttribute("moduleType", moduleType);
        if ("forum".equals(moduleType) && instanceId != null) {
            List<ForumDiscussionDTO> forumDiscussions = myCoursesStudentService.fetchForumContent(instanceId);
            model.addAttribute("forumDiscussions", forumDiscussions); // Add discussions to the model
        }
        return "student/module_details"; // Trả về view
    }



    @GetMapping("/user/quizCourseStudent")
    public String showQuizDetailsGet(@RequestParam("quizId") Integer quizId,
                                     @RequestParam("moodleCourseId") Integer moodleCourseId,
                                     @RequestParam("userId") Integer userId,
                                     Model model) {

        // In ra để kiểm tra xem quizId và userId có được truyền đúng không
        System.out.println("GET - Quiz ID: " + quizId + ", User ID: " + userId);

        // Lấy chi tiết quiz bao gồm maxGrade và numberOfQuestions
        QuizDetails quizDetails = myCoursesStudentService.getQuizDetails(quizId.toString(), moodleCourseId);
        Double maxGrade = quizDetails.getMaxGrade();
        Integer numberOfQuestions = quizDetails.getNumberOfQuestions();

        if (maxGrade == null || numberOfQuestions == null) {
            System.out.println("Max grade or number of questions not found for course ID: " + moodleCourseId);
            model.addAttribute("errorMessage", "Could not retrieve quiz details.");
            return "student/quiz_details"; // Trả về view với thông báo lỗi
        }

        // Lấy danh sách attempts của sinh viên cho quiz
        List<AttemptIDTO> attempts = myCoursesStudentService.getStudentAttempts(userId.toString(), quizId.toString(), maxGrade, numberOfQuestions);

        // Thêm attempts vào model để hiển thị trên giao diện
        model.addAttribute("quizId", quizId);
        model.addAttribute("userId", userId);
        model.addAttribute("quizDetails", attempts); // Đổi từ 'attempts' thành 'quizDetails'
        model.addAttribute("maxGrade", maxGrade); // Thêm maxGrade vào model nếu cần
        model.addAttribute("numberOfQuestions", numberOfQuestions); // Thêm số câu hỏi vào model nếu cần

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


}
