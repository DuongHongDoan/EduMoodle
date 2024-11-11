package com.example.edumoodle.Controller;

import com.example.edumoodle.DTO.*;
import com.example.edumoodle.Model.*;
import com.example.edumoodle.Repository.*;
import com.example.edumoodle.Service.CategoriesService;
import com.example.edumoodle.Service.CoursesService;
import com.example.edumoodle.Service.QuizService;
import com.example.edumoodle.Service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    private QuizService quizService;

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

        System.out.println("Type file đầu vào: " + type);

        String fileType = file.getContentType();
        System.out.println("Kq biến fileType: " + fileType);
        if (!"text/csv".equals(fileType)
                && !"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(fileType)
                && !"application/vnd.ms-excel".equals(fileType)) {
            model.addAttribute("errorMessage", "Chỉ chấp nhận file định dạng CSV hoặc Excel");
            return "common/UploadEnrolUsers";
        }

        try {
            System.out.println("Bắt đầu parse file");
            // Đọc và xử lý file CSV
            List<EnrolUserDTO> enrolUsers = usersService.parseFileEnrolUsers(file, type);
            System.out.println("bắt đầu vòng for");

            for (EnrolUserDTO enrolUser : enrolUsers) {
                usersService.enrolUser(enrolUser);

                for(Integer userId : enrolUser.getUserid()) {
                    UsersEntity user = usersRepository.findByMoodleId(userId).orElse(null);
                    if (user == null) {
                        model.addAttribute("errorMessage", "Người dùng không tồn tại." + userId);
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

    // url = /manage/courses/report?courseId=&quizId= --> trả ra trang kết quả điểm của một quiz
    @GetMapping("/manage/courses/report")
    public String getReportGradeOfQuiz(@RequestParam Integer courseId, @RequestParam Integer quizId,
                                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                                       @RequestParam(value = "size", defaultValue = "30") Integer size, Model model) {
        List<QuizAttemptListDTO.AttemptDTO> attempts = quizService.getAllAttemptStudents(quizId, courseId);
        model.addAttribute("attempts", attempts);
        int attemptCnt = attempts.size();
        model.addAttribute("attemptCnt", attemptCnt);

        List<UsersDTO> usersEnrolled = usersService.getEnrolledUsers(courseId);
        List<UsersDTO> studentsEnrolled = usersEnrolled.stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.getRoleid() == 5))
                .toList();
        model.addAttribute("userEnrolledCnt", studentsEnrolled.size());

        //kiểm tra số lượng attempts để phân trang
        if(attemptCnt > size) {
            int pageIndex = page - 1;
            int start = pageIndex * size;
            int end = Math.min(start + size, attemptCnt);
            List<QuizAttemptListDTO.AttemptDTO> pagedCourses = attempts.subList(start, end);
            Page<QuizAttemptListDTO.AttemptDTO> attemptPage = new PageImpl<>(pagedCourses, PageRequest.of(pageIndex, size), attemptCnt);
            model.addAttribute("attemptPage", attemptPage);
        } else {
            model.addAttribute("attemptPage", null);
        }

        QuizzesDTO.QuizzesListDTO quiz = quizService.getQuizInCourse(quizId, courseId);
        model.addAttribute("quiz", quiz);

        model.addAttribute("courseId", courseId);
        model.addAttribute("quizId", quizId);
        return "common/ResultGrade";
    }

    //url = /manage/attempts/search --> tìm kiếm bài thi theo tên sinh viên đăng ký vào khóa học
    @GetMapping("/manage/attempts/search")
    public String searchAttempts(@RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam Integer courseId, @RequestParam Integer quizId, Model model) {
        List<QuizAttemptListDTO.AttemptDTO> attempts;
        if(keyword != null && !keyword.isEmpty()) {
            attempts = quizService.getSearchAttemptByStudentName(keyword, courseId, quizId);
        } else {
            attempts = List.of();
        }
        model.addAttribute("attempts", attempts);
        List<QuizAttemptListDTO.AttemptDTO> attemptsAll = quizService.getAllAttemptStudents(quizId, courseId);
        int attemptCnt = attemptsAll.size();
        model.addAttribute("attemptCnt", attemptCnt);

        List<UsersDTO> usersEnrolled = usersService.getEnrolledUsers(courseId);
        List<UsersDTO> studentsEnrolled = usersEnrolled.stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.getRoleid() == 5))
                .toList();
        model.addAttribute("userEnrolledCnt", studentsEnrolled.size());

        QuizzesDTO.QuizzesListDTO quiz = quizService.getQuizInCourse(quizId, courseId);
        model.addAttribute("quiz", quiz);

        model.addAttribute("courseId", courseId);
        model.addAttribute("quizId", quizId);

        model.addAttribute("keyword", keyword);
        return "common/ResultGrade";
    }

    //url = /manage/courses/export?courseId=&quizId= --> xuất file điểm excel của 1 bài quiz
    @GetMapping("/manage/courses/export")
    public ResponseEntity<Resource> exportAttemptListResult(@RequestParam Integer courseId, @RequestParam Integer quizId) {
        List<QuizAttemptListDTO.AttemptDTO> attempts = quizService.getAllAttemptStudents(quizId, courseId);
        QuizzesDTO.QuizzesListDTO quiz = quizService.getQuizInCourse(quizId, courseId);
        List<UsersDTO> usersEnrolled = usersService.getEnrolledUsers(courseId);
        List<UsersDTO> studentsEnrolled = usersEnrolled.stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.getRoleid() == 5))
                .toList();

        try {
            // Đường dẫn file tạm thời trong server
            String tempFilePath = System.getProperty("java.io.tmpdir") + "exported_results.xlsx";
            quizService.exportAttemptToExcel(quiz, attempts, studentsEnrolled, tempFilePath, courseId);

            // Tạo InputStreamResource từ file để gửi phản hồi
            File file = new File(tempFilePath);
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            // Tạo response với header Content-Disposition
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(file.length())
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    //url = /manage/courses/review?attempt= --> hiển thị bài thi chi tiết của từng sv
    @GetMapping("/manage/courses/review")
    public String getAttemptDetail(@RequestParam Integer attemptId, @RequestParam Integer courseId,
                                   @RequestParam Integer quizId, Model model) {
        AttemptViewDTO questionsDetail = quizService.getAttemptDetailInfo(attemptId, courseId, quizId);
        model.addAttribute("questionsDetail", questionsDetail);

        QuizzesDTO.QuizzesListDTO quiz = quizService.getQuizInCourse(quizId, courseId);
        model.addAttribute("quiz", quiz);

        // Gọi service để lấy thông tin chi tiết của quiz dựa trên attemptId
        List<QuestionDetail> questionDetails = quizService.getAttemptDetails(attemptId);

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

        return "common/AttemptDetail";
    }
}
