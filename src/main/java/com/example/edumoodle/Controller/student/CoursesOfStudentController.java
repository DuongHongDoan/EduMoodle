package com.example.edumoodle.Controller.student;

import com.example.edumoodle.DTO.*;
import com.example.edumoodle.Service.CoursesService;
import com.example.edumoodle.Service.MyCoursesStudentService;
import com.example.edumoodle.Service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
public class CoursesOfStudentController {

//    @GetMapping("/user/quizCourseStudent")
//    public String getMyCourse(Model model) {
//        return "student/quiz_details";
//    }
    @Autowired
    private MyCoursesStudentService myCoursesStudentService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private CoursesService coursesService;

    //hiển thị danh sách các khóa học của sinh viên đã tham gia
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
        System.out.println("Username: " + username);

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

    //tìm kiếm khóa học của sinh viên đã tham gia
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
        System.out.println("Username: " + username);

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
        List<SectionsDTO> sections = coursesService.getCourseContent(moodleCourseId);
        model.addAttribute("sections", sections);

        CoursesDTO courseDetails = coursesService.getCourseDetail(moodleCourseId);
        model.addAttribute("courseDetails", courseDetails);
        List<UsersDTO> enrolledUsers = usersService.getEnrolledUsers(moodleCourseId);
        model.addAttribute("enrolledUsers", enrolledUsers);
        long studentCount = enrolledUsers.stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> "student".equals(role.getShortname())))
                .count();
        model.addAttribute("studentCount", studentCount);

        List<UsersDTO> usersList = usersService.getAllUsers();
        // Lọc ra những sinh viên chưa đăng ký khóa học
        List<UsersDTO> usersListNotEnrolled = usersList.stream()
                .filter(user -> user.getId() != 1 && user.getId() != 2) // Loại bỏ các user ID không liên quan
                .filter(user -> enrolledUsers.stream().noneMatch(enrolledUser -> enrolledUser.getId().equals(user.getId())))
                .toList();
        model.addAttribute("usersList", usersListNotEnrolled);
        model.addAttribute("course", moodleCourseId);

        List<ModuleDTO> moduleList = coursesService.getModuleList();
        model.addAttribute("moduleList", moduleList);

        //----------------------------------------------
        // Lấy tên người dùng hiện tại từ Spring Security
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Lấy userId dựa trên username
        UsersDTO usersDTO = usersService.getUserByUsername(username);
        Integer userId = usersDTO.getId();

        //Lấy thông tin khóa học đã đc truy cập gần đây
        CoursesDTO courseDetails2 = myCoursesStudentService.getCourseDetails(moodleCourseId);
        String courseName = courseDetails2.getFullname(); // Tên khóa học
        String categoryName = courseDetails2.getCategoryName(); // Tên danh mục
        String instructorName = courseDetails2.getTeacherName(); // Tên giảng viên
        myCoursesStudentService.saveAccessedCourse(userId, moodleCourseId, courseName, categoryName, instructorName);

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

        return "admin/DetailCourse"; // Trả về tên view
    }

    //xem chi tiết các bài thi của 1 tài khoản sinh viên
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

    //Xem lại bài thi chi tiết
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
}
