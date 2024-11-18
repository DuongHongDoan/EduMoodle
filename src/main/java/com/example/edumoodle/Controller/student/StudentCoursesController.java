package com.example.edumoodle.Controller.student;

import com.example.edumoodle.DTO.*;
import com.example.edumoodle.Model.RecentlyAccessedCoursesEntity;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class StudentCoursesController {

    @Autowired
    private MyCoursesStudentService myCoursesStudentService;

    @GetMapping("/user/my-student-courses")
    public String showStudentCourses(Model model) {
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

        // Kiểm tra nếu không có username
        if (username == null) {
            model.addAttribute("error", "Không thể xác định được người dùng hiện tại.");
            return "student/mystudent_courses";
        }

        // Lấy danh sách khóa học của người dùng
        List<CoursesDTO> userCourses = myCoursesStudentService.getUserCourses(username);
        model.addAttribute("username", username);
        model.addAttribute("userCourses", userCourses);

        return "student/mystudent_courses";
    }

    @PostMapping("/user/my-courses-student")
    public String processCourseSearch(@RequestParam(value = "searchQuery", required = false) String searchQuery,
                                      @RequestParam(value = "sort", required = false) String sort,
                                      @RequestParam(value = "showAll", required = false) String showAll,
                                      Model model) {
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

        if (username == null) {
            model.addAttribute("error", "Không thể xác định được người dùng hiện tại.");
            return "student/mystudent_courses";
        }

        // Lấy tất cả khóa học của người dùng
        List<CoursesDTO> userCourses = myCoursesStudentService.getUserCourses(username);

        // Kiểm tra nếu "Hiển thị tất cả" được chọn
        if (showAll != null && showAll.equals("true")) {
            // Nếu được chọn, bỏ qua phần tìm kiếm
            searchQuery = null;
        }

        // Nếu không chọn "Hiển thị tất cả" thì lọc theo từ khóa tìm kiếm
        if (searchQuery != null && !searchQuery.isEmpty()) {
            userCourses = myCoursesStudentService.filterCoursesByName(userCourses, searchQuery);
            if (userCourses.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy khóa học phù hợp.");
            }
        }

        // Sắp xếp theo tên nếu chọn
        if ("name".equals(sort)) {
            userCourses.sort(Comparator.comparing(CoursesDTO::getFullname, String.CASE_INSENSITIVE_ORDER));
        }

        // Cập nhật lại model với các khóa học sau khi lọc và sắp xếp
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("userCourses", userCourses);

        return "student/mystudent_courses";
    }



    // Lấy chi tiết một khóa học dựa trên moodleCourseId
    @GetMapping("/user/student_course_details")
    public String getCourseDetails(@RequestParam("moodleCourseId") Integer moodleCourseId, Model model) {
        // Lấy tên người dùng hiện tại từ Spring Security
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Lấy userId dựa trên username
        int userId = myCoursesStudentService.getUserIdByUsername(username);

        // Log chi tiết để gỡ lỗi
        System.out.println("Username: " + username);
        System.out.println("UserID (GET): " + userId);
        System.out.println("Moodle Course ID: " + moodleCourseId);

        // Lấy thông tin chi tiết khóa học dựa trên moodleCourseId
        CoursesDTO courseDetails = myCoursesStudentService.getCourseDetails(moodleCourseId);
        // Kiểm tra nếu courseDetails không phải là null
        if (courseDetails != null) {
            // Log thông tin khóa học
            System.out.println("Course Details: " + courseDetails);

            // Lấy thông tin chi tiết từ CoursesDTO
            String courseName = courseDetails.getFullname(); // Tên khóa học
            String categoryName = courseDetails.getCategoryName(); // Tên danh mục
            String instructorName = courseDetails.getTeacherName(); // Tên giảng viên

            // Log tên giảng viên để kiểm tra giá trị
            System.out.println("Instructor Name: " + instructorName);

            // Lưu thông tin khóa học đã truy cập gần đây
            myCoursesStudentService.saveAccessedCourse(userId, moodleCourseId, courseName, categoryName, instructorName);

            // Thêm thông tin chi tiết khóa học và các thuộc tính khác vào model
            model.addAttribute("courseDetails", courseDetails);
            model.addAttribute("userId", userId);
            model.addAttribute("moodleCourseId", moodleCourseId);
            model.addAttribute("instructorName", instructorName); // Thêm tên giảng viên vào model
        } else {
            // Nếu courseDetails là null, log thông báo lỗi
            System.out.println("Không tìm thấy thông tin khóa học cho Moodle Course ID: " + moodleCourseId);
            // Xử lý lỗi hoặc thông báo người dùng về việc không tìm thấy khóa học
            model.addAttribute("errorMessage", "Không tìm thấy thông tin khóa học.");
        }

        return "student/mystudent_course_details"; // Trả về tên view
    }


    // danh sach sinh vien trong khoa hoc
    @GetMapping("/user/student_list")
    public String getStudentList(@RequestParam("moodleCourseId") Integer moodleCourseId,
                                 @RequestParam(value = "userId", required = false) Integer userId,
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
        model.addAttribute("userId", userId); // Thêm userId vào model
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

        // Thêm dữ liệu vào model để hiển thị trong view
        model.addAttribute("quizId", quizId);
        model.addAttribute("userId", userId);
        model.addAttribute("quizDetails", attempts); // Thêm danh sách attempts vào model
        model.addAttribute("maxGrade", maxGrade); // Thêm điểm tối đa
        model.addAttribute("numberOfQuestions", numberOfQuestions); // Thêm số câu hỏi

        // Gọi hàm getAttemptId từ service để lấy attemptId
//        String attemptId = myCoursesStudentService.getAttemptId(quizId.toString(), userId.toString());
//        model.addAttribute("attemptId", attemptId); // Thêm attemptId vào model nếu cần

        // Trả về view 'student/quiz_details' để hiển thị chi tiết quiz
        return "student/quiz_details";
    }

//    @GetMapping ("/user/quiz/start")
//    public String startQuiz(@RequestParam("quizId") Integer quizId,
//                            @RequestParam("userId") Integer userId,
//                            @RequestParam("attemptId") String attemptId,
//                            Model model) {
//
//        // In ra để kiểm tra tham số nhận được
//        System.out.println("GET - Start quiz with Quiz ID: " + quizId + ", User ID: " + userId + ", Attempt ID: " + attemptId);
//
//        // Trả về view để hiển thị kết quả
//        return "student/quiz_test_data";
//    }

    // xem lai bai kem tra
    @GetMapping("/user/quiz/review")
    public String reviewQuiz() {

        return "student/quiz_review"; // Trả về view "quiz_review.html"
    }

    @PostMapping("/user/quiz/review")
    public String reviewQuizAttempt(
            @RequestParam(value = "attemptId", required = true) int attemptId,
            @RequestParam(value = "maxGrade", required = true) Double maxGrade,
            @RequestParam(value = "numberOfQuestions", required = true) Integer numberOfQuestions,
            @RequestParam(value = "score", required = true) Double score,
            @RequestParam(value = "startTime", required = true) String startTime,
            @RequestParam(value = "finishTime", required = true) String finishTime,
            @RequestParam(value = "status", required = true) String status,
            @RequestParam(value = "calculatedScore", required = false) Double calculatedScore,
            @RequestParam(value = "formattedScore", required = false) String formattedScore,
            @RequestParam(value = "scoreMaxGrade", required = false) String scoreMaxGrade,
            Model model) {

        // Gọi service để lấy thông tin chi tiết của quiz dựa trên attemptId
        List<QuestionDetail> questionDetails = myCoursesStudentService.getAttemptDetails(attemptId);

        // Tính số câu đúng và sai
        long correctCount = questionDetails.stream().filter(QuestionDetail::isCorrect).count();
        long totalQuestions = questionDetails.size();
        long incorrectCount = totalQuestions - correctCount;

        // Tính thời gian đã trôi qua
        Duration timeTaken = Duration.between(LocalDateTime.parse(startTime), LocalDateTime.parse(finishTime));
        String formattedTimeTaken = formatDuration(timeTaken);

        // Thêm các thông tin vào model để hiển thị trên view
        model.addAttribute("questionDetails", questionDetails);
        model.addAttribute("correctCount", correctCount);
        model.addAttribute("incorrectCount", incorrectCount);
        model.addAttribute("totalQuestions", totalQuestions);

        // Thêm thông tin quiz (maxGrade, numberOfQuestions, score, etc.)
        model.addAttribute("attemptId", attemptId);
        model.addAttribute("maxGrade", maxGrade);
        model.addAttribute("numberOfQuestions", numberOfQuestions);
        model.addAttribute("score", score);
        model.addAttribute("startTime", startTime);
        model.addAttribute("finishTime", finishTime);
        model.addAttribute("status", status);
        model.addAttribute("timeTaken", formattedTimeTaken); // Thêm thời gian đã trôi qua

        // Thêm thông tin tính toán đã truyền từ form
        model.addAttribute("calculatedScore", calculatedScore);
        model.addAttribute("formattedScore", formattedScore);
        model.addAttribute("scoreMaxGrade", scoreMaxGrade);

        // Trả về trang review để hiển thị chi tiết bài kiểm tra
        return "student/quiz_review";
    }

    // Hàm định dạng thời gian đã trôi qua thành chuỗi dễ đọc
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }



    //Trang ca nhan
    @GetMapping("/user/my_recent_courses")
    public String showRecentlyAccessedCoursesPage(Model model) {
        // Get the current user's username from Spring Security
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Retrieve userId based on the username
        Integer userId = myCoursesStudentService.getUserIdByUsername(username);

        // Get the list of recently accessed courses
        List<RecentlyAccessedCourseDTO> recentlyAccessedCourses = myCoursesStudentService.getRecentlyAccessedCourses(userId);

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
