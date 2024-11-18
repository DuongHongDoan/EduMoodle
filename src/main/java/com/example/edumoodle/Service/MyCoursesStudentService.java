package com.example.edumoodle.Service;
import com.example.edumoodle.DTO.*;
import com.example.edumoodle.Model.RecentlyAccessedCoursesEntity;
import com.example.edumoodle.Repository.CategoriesRepository;
import com.example.edumoodle.Repository.CoursesRepository;
import com.example.edumoodle.Repository.RecentlyAccessedCoursesRepository;
import jakarta.transaction.Transactional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import org.jsoup.nodes.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Service
public class MyCoursesStudentService {

    @Value("${moodle.token}")
    private String token;

    @Value("${moodle.domainName}")
    private String domainName;

    @Autowired
    private CoursesRepository repo;
    @Autowired
    private CategoriesRepository categoriesRepository;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RecentlyAccessedCoursesRepository recentlyAccessedCoursesRepository;

    //gioi han hien thi cac khoa hoc truy cap
    private static final int MAX_RECENT_COURSES = 10; // Giới hạn tối đa


    public List<CoursesDTO> getUserCourses(String username) {
        List<CoursesDTO> userCourses = new ArrayList<>(); // Danh sách khóa học của người dùng
        int userId = getUserIdByUsername(username); // Lấy user ID từ username

        // Đảm bảo user ID hợp lệ
        if (userId == 0) {
            return userCourses; // Trả về danh sách trống nếu không tìm thấy user ID
        }

        // URL API để lấy khóa học của người dùng
        String getUserCoursesUrl = domainName + "/webservice/rest/server.php" +
                "?wstoken=" + token +
                "&wsfunction=core_enrol_get_users_courses" +
                "&moodlewsrestformat=json" +
                "&userid=" + userId;

        System.out.println("userid: " + userId);
        String coursesResponse = restTemplate.getForObject(getUserCoursesUrl, String.class);

        // Phân tích dữ liệu khóa học từ phản hồi
        userCourses = parseCoursesResponse(coursesResponse);

        return userCourses; // Trả về danh sách khóa học của người dùng
    }

    public int getUserIdByUsername(String username) {
        String getStudentsFunction = "core_user_get_users";

        // URL API để lấy user ID dựa vào username
        String getStudentsUrl = domainName + "/webservice/rest/server.php" +
                "?wstoken=" + token +
                "&wsfunction=" + getStudentsFunction +
                "&moodlewsrestformat=json" +
                "&criteria[0][key]=username" +
                "&criteria[0][value]=" + username;

        // Gửi yêu cầu GET tới API của Moodle
        String studentsResponse = restTemplate.getForObject(getStudentsUrl, String.class);
        System.out.println("Username: " + username);
        int userId = 0;

        // Trích xuất user ID từ phản hồi của API
        try {
            JSONObject jsonObject = new JSONObject(studentsResponse);
            if (jsonObject.has("users")) {
                JSONArray users = jsonObject.getJSONArray("users");
                if (users.length() > 0) {
                    JSONObject user = users.getJSONObject(0);
                    userId = user.getInt("id");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return userId; // Trả về user ID
    }

    private List<CoursesDTO> parseCoursesResponse(String coursesResponse) {
        List<CoursesDTO> userCourses = new ArrayList<>();
        try {
            JSONArray coursesArray = new JSONArray(coursesResponse);
            for (int i = 0; i < coursesArray.length(); i++) {
                JSONObject course = coursesArray.getJSONObject(i);
                CoursesDTO coursesDto = new CoursesDTO();
                int courseMoodleId = course.getInt("id");  // Lấy courseMoodleId
                String courseName = course.getString("fullname");
                int category = course.optInt("category", 0); // Mặc định là 0 nếu không có

                // Lấy tên danh mục
                String categoryName = course.optString("categoryname", getCategoryName(category));

                // Gọi API để lấy thông tin giảng viên của khóa học
                String teacherName = getTeacherName(courseMoodleId);

                // Thiết lập thông tin vào đối tượng CoursesDTO
                coursesDto.setMoodleCourseId(courseMoodleId);
                coursesDto.setFullname(courseName);
                coursesDto.setCategoryid(category); // Set giá trị category
                coursesDto.setCategoryName(categoryName); // Set giá trị category name
                coursesDto.setTeacherName(teacherName); // Set giá trị tên giảng viên

                userCourses.add(coursesDto);
                System.out.println("Course Moodle ID: " + courseMoodleId + ", Course Name: " + courseName +
                        ", Category: " + category + ", Category Name: " + categoryName + ", Teacher: " + teacherName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return userCourses;
    }

    public String getTeacherName(int courseMoodleId) {
        String getEnrolledUsersFunction = "core_enrol_get_enrolled_users";

        // URL API để lấy thông tin giảng viên của khóa học
        String getEnrolledUsersUrl = domainName + "/webservice/rest/server.php" +
                "?wstoken=" + token +
                "&wsfunction=" + getEnrolledUsersFunction +
                "&moodlewsrestformat=json" +
                "&courseid=" + courseMoodleId;

        String enrolledUsersResponse = restTemplate.getForObject(getEnrolledUsersUrl, String.class);
        String teacherName = "Không xác định"; // Mặc định là không xác định nếu không tìm thấy giảng viên

        try {
            JSONArray enrolledUsersArray = new JSONArray(enrolledUsersResponse);
            for (int j = 0; j < enrolledUsersArray.length(); j++) {
                JSONObject user = enrolledUsersArray.getJSONObject(j);
                if (user.has("roles")) {
                    JSONArray roles = user.getJSONArray("roles");
                    for (int k = 0; k < roles.length(); k++) {
                        JSONObject role = roles.getJSONObject(k);
                        // Kiểm tra vai trò của người dùng, tìm vai trò giảng viên (editingteacher hoặc teacher)
                        String roleShortName = role.getString("shortname");
                        if (roleShortName.equals("editingteacher") || roleShortName.equals("teacher")) {
                            teacherName = user.getString("fullname"); // Lấy tên giảng viên
                            break; // Dừng khi tìm thấy giảng viên đầu tiên
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return teacherName; // Trả về tên giảng viên
    }


    // Hàm để lấy tên của category dựa vào category ID
    public String getCategoryName(int categoryId) {
        String getCategoryFunction = "core_course_get_categories";
        String getCategoryUrl = domainName + "/webservice/rest/server.php" +
                "?wstoken=" + token +
                "&wsfunction=" + getCategoryFunction +
                "&moodlewsrestformat=json" +
                "&criteria[0][key]=id" +
                "&criteria[0][value]=" + categoryId;

        String categoryResponse = restTemplate.getForObject(getCategoryUrl, String.class);

        try {
            JSONArray categoryArray = new JSONArray(categoryResponse);
            if (categoryArray.length() > 0) {
                JSONObject category = categoryArray.getJSONObject(0);
                return category.getString("name"); // Lấy tên category
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "Không xác định"; // Trả về "Không xác định" nếu không tìm thấy tên category
    }

    // Hàm lọc khóa học theo tên
    public List<CoursesDTO> filterCoursesByName(List<CoursesDTO> courses, String searchQuery) {
        List<CoursesDTO> filteredCourses = new ArrayList<>();

        for (CoursesDTO course : courses) {
            // Kiểm tra xem fullname có chứa searchQuery không (không phân biệt hoa thường)
            if (course.getFullname().toLowerCase().contains(searchQuery.toLowerCase())) {
                filteredCourses.add(course);
            }
        }

        return filteredCourses;
    }
    //chitieetsts khóa học
    public CoursesDTO getCourseDetails(Integer moodleCourseId) {
        CoursesDTO courseDetails = new  CoursesDTO();
        List<SectionsDTO> sectionsList = new ArrayList<>(); // Lưu trữ các section

        // Thiết lập Moodle Course ID cho CoursesDto
        courseDetails.setMoodleCourseId(moodleCourseId);

        // Step 1: Fetch course details (including category) from the core_course_get_courses API
        String getCourseDetailsUrl = domainName + "/webservice/rest/server.php" +
                "?wstoken=" + token +
                "&wsfunction=core_course_get_courses" +
                "&moodlewsrestformat=json" +
                "&options[ids][0]=" + moodleCourseId;

        String courseDetailsResponse = restTemplate.getForObject(getCourseDetailsUrl, String.class);
        System.out.println(getCourseDetailsUrl);  // Kiểm tra URL

        if (courseDetailsResponse != null && !courseDetailsResponse.isEmpty()) {
            System.out.println(courseDetailsResponse); // Kiểm tra response từ Moodle
            try {
                // Parse the course details JSON response
                JSONArray coursesArray = new JSONArray(courseDetailsResponse);
                if (coursesArray.length() > 0) {
                    JSONObject course = coursesArray.getJSONObject(0); // Get the first (and only) course

                    // Set the actual details from the API response
                    courseDetails.setFullname(course.getString("fullname"));
                    courseDetails.setShortname(course.optString("shortname", "No shortname available"));
                    courseDetails.setDescription(course.optString("summary", "No description available"));

                    // Fetch and set category information
                    Integer categoryId = course.optInt("categoryid", 0);
                    courseDetails.setCategoryid(categoryId);  // Set category ID

                    // Sử dụng phương thức getCategoryName để lấy tên danh mục
                    if (categoryId != 0) {
                        String categoryName = getCategoryName(categoryId);  // Gọi phương thức đã có
                        courseDetails.setCategoryName(categoryName); // Set category name
                    } else {
                        courseDetails.setCategoryName("No category available");
                    }
                }

            } catch (JSONException e) {
                System.out.println("Error parsing course details JSON");
                e.printStackTrace();
            }
        } else {
            System.out.println("No course details returned from API");
        }

        // Step 2: Fetch course contents (sections and modules) from core_course_get_contents API
        String getCourseContentsUrl = domainName + "/webservice/rest/server.php" +
                "?wstoken=" + token +
                "&wsfunction=core_course_get_contents" +
                "&moodlewsrestformat=json" +
                "&courseid=" + moodleCourseId;

        String courseContentsResponse = restTemplate.getForObject(getCourseContentsUrl, String.class);
        System.out.println(getCourseContentsUrl);

        if (courseContentsResponse != null && !courseContentsResponse.isEmpty()) {
            System.out.println(courseContentsResponse);
            try {
                // Parse JSON response for sections and modules
                JSONArray sections = new JSONArray(courseContentsResponse);
                for (int i = 0; i < sections.length(); i++) {
                    JSONObject section = sections.getJSONObject(i);
                    String sectionName = section.optString("name", "No name");

                    SectionsDTO sectionDto = new SectionsDTO();
                    sectionDto.setName(sectionName);

                    List<ModuleDTO> moduleList = new ArrayList<>();

                    JSONArray modules = section.optJSONArray("modules");
                    if (modules != null) {
                        for (int j = 0; j < modules.length(); j++) {
                            JSONObject module = modules.getJSONObject(j);
                            String moduleName = module.optString("name", "No name");
                            Integer moduleId = module.optInt("id");
                            String moduleType = module.optString("modname", "unknown"); // Get module type

                            ModuleDTO moduleDto = new ModuleDTO();
                            moduleDto.setId(moduleId);
                            moduleDto.setName(moduleName);
                            moduleDto.setModuleType(moduleType);

                            // Check if the module is a forum, and handle instanceId
                            if ("forum".equals(moduleType)) {
                                Integer instanceId = module.optInt("instance", 0); // Get instance ID (forum ID)
                                moduleDto.setInstanceId(instanceId); // Set instance ID only for forums
                                System.out.println("Forum ID (Instance): " + instanceId);
                            }

                            // Print module info (without instanceId for other types)
                            System.out.println("Module ID: " + moduleId + ", Name: " + moduleName + ", Type: " + moduleType);

                            moduleList.add(moduleDto);
                        }
                    }

                    sectionDto.setModuless(moduleList);
                    sectionsList.add(sectionDto);
                }
            } catch (JSONException e) {
                System.out.println("Error parsing course contents JSON");
                e.printStackTrace();
            }
        }

        courseDetails.setSections(sectionsList);

        return courseDetails;
    }

    // Phương thức để lấy nội dung module dựa trên module ID và loại
    public Object fetchModuleContent(Integer moduleId, String moduleType, Integer moodleCourseId) {
        String moduleContentUrl;

        // Xây dựng URL cho các loại module khác nhau
        moduleContentUrl = domainName + "/webservice/rest/server.php" +
                "?wstoken=" + token +
                "&wsfunction=" + getModuleFunction(moduleType) +
                "&moodlewsrestformat=json" +
                "&courseids[0]=" + moodleCourseId;  // Sử dụng moodleCourseId cho tất cả loại module

        // Gọi API để lấy nội dung của module
        String response = restTemplate.getForObject(moduleContentUrl, String.class);
        System.out.println("API URL: " + moduleContentUrl); // Kiểm tra URL gọi API
        System.out.println("API Response: " + response);   // Kiểm tra phản hồi từ API

        // Xử lý phản hồi JSON
        try {
            JSONObject jsonResponse = new JSONObject(response);

            // Kiểm tra mã lỗi từ phản hồi
            if (jsonResponse.has("errorcode")) {
                return "Lỗi từ API: " + jsonResponse.optString("error");
            }

            // Kiểm tra loại module và xử lý phù hợp
            if (moduleType.equals("resource") && jsonResponse.has("resources")) {
                return handleResources(jsonResponse, moduleId);  // Gọi handleResources với moduleId
            } else if (jsonResponse.has("assignments")) {
                return handleAssignments(jsonResponse);  // Xử lý bài tập (assignments)
            } else if (jsonResponse.has("quizzes")) {
                return handleQuizzes(jsonResponse, moduleId);  // Trả về danh sách bài kiểm tra
            }

            // Thêm các trường hợp khác cho các loại module khác nếu cần

        } catch (JSONException e) {
            System.err.println("Error parsing JSON response: " + e.getMessage());
        }

        // Nếu không có dữ liệu hợp lệ, trả về thông báo mặc định
        return "No content available for this module.";
    }

    // Phương thức để xử lý thông tin assignments
    private String handleAssignments(JSONObject jsonResponse) throws JSONException {
        JSONArray assignments = jsonResponse.getJSONArray("assignments");
        if (assignments.length() > 0) {
            JSONObject assignment = assignments.getJSONObject(0);
            String assignmentDetails = "Assignment: " + assignment.optString("name") + "\nDetails: " + assignment.optString("intro");

            // Kiểm tra nếu có file đính kèm
            if (assignment.has("introattachments")) {
                JSONArray attachments = assignment.getJSONArray("introattachments");
                if (attachments.length() > 0) {
                    JSONObject attachment = attachments.getJSONObject(0); // Lấy file đầu tiên
                    String fileUrl = attachment.optString("fileurl"); // Lấy URL file

                    // Thêm token vào URL file
                    if (fileUrl != null && !fileUrl.isEmpty()) {
                        fileUrl += "?token=" + token; // Thêm token vào cuối URL
                        assignmentDetails += "\nFile URL: " + fileUrl; // Thêm URL file vào thông tin bài tập
                    }
                }
            }

            return assignmentDetails;
        }
        return "No assignments found.";
    }


    // Phương thức để xử lý thông tin quizzes và trả về danh sách đối tượng QuizDTO
    private List<QuizDTO> handleQuizzes(JSONObject jsonResponse, Integer selectedModuleId) throws JSONException {
        JSONArray quizzes = jsonResponse.getJSONArray("quizzes");
        List<QuizDTO> quizList = new ArrayList<>();

        if (quizzes.length() > 0) {
            // Duyệt qua danh sách các bài kiểm tra
            for (int i = 0; i < quizzes.length(); i++) {
                JSONObject quiz = quizzes.getJSONObject(i);
                int quizId = quiz.getInt("id");
                String quizName = quiz.optString("name");
                int moduleId = quiz.optInt("coursemodule"); // Lấy moduleId từ JSON nếu có

                // Kiểm tra nếu moduleId đã được chọn và chỉ lấy module đó
                if (selectedModuleId != null && moduleId != selectedModuleId) {
                    continue; // Bỏ qua các quiz không thuộc module được chọn
                }

                // Tạo đối tượng QuizDTO và thêm vào danh sách
                QuizDTO quizInfo = new QuizDTO(quizId, quizName, moduleId);
                quizList.add(quizInfo);

                System.out.println("Quiz ID: " + quizId + ", Quiz Name: " + quizName + ", Module ID: " + moduleId);
            }
        } else {
            System.out.println("No quizzes found.");
        }
        return quizList;
    }


    // Phương thức để xử lý tất cả resources theo module và chỉ lấy theo moduleId nếu cần
    private String handleResources(JSONObject jsonResponse, Integer selectedModuleId) throws JSONException {
        JSONArray resources = jsonResponse.getJSONArray("resources");
        StringBuilder moduleHtml = new StringBuilder();

        if (resources.length() > 0) {
            Map<Integer, StringBuilder> moduleMap = new HashMap<>();

            for (int i = 0; i < resources.length(); i++) {
                JSONObject resource = resources.getJSONObject(i);
                int moduleId = resource.getInt("coursemodule");
                String resourceName = resource.optString("name");

                // In moduleId ra console để kiểm tra
                System.out.println("Module ID: " + moduleId);

                // Kiểm tra nếu moduleId đã được chọn và chỉ lấy module đó
                if (selectedModuleId != null && moduleId != selectedModuleId) {
                    continue; // Bỏ qua các module không được chọn
                }

                // Thay vì in ra moduleId, chỉ hiển thị tên của resource
                if (!moduleMap.containsKey(moduleId)) {
                    moduleMap.put(moduleId, new StringBuilder()); // Xóa phần hiển thị moduleId trên giao diện
                }

                moduleMap.get(moduleId).append("<h3>").append(resourceName).append("</h3>");

                JSONArray contentFiles = resource.getJSONArray("contentfiles");
                if (contentFiles.length() > 0) {
                    for (int j = 0; j < contentFiles.length(); j++) {
                        JSONObject file = contentFiles.getJSONObject(j);
                        String fileUrl = file.optString("fileurl");
                        String fileName = file.optString("filename");

                        if (fileUrl.contains("/webservice")) {
                            fileUrl = fileUrl.replace("/webservice", "");
                        }

                        if (!fileUrl.isEmpty()) {
                            if (fileUrl.contains("?")) {
                                fileUrl += "&wstoken=" + token;
                            } else {
                                fileUrl += "?wstoken=" + token;
                            }

                            moduleMap.get(moduleId).append("<a href=\"").append(fileUrl).append("\" target=\"_blank\">")
                                    .append("Xem tài liệu: ").append(fileName).append("</a><br>");
                        }
                    }
                } else {
                    moduleMap.get(moduleId).append("Không tìm thấy tài liệu cho tài nguyên này.<br>");
                }

                moduleMap.get(moduleId).append("<hr>");
            }

            for (StringBuilder moduleContent : moduleMap.values()) {
                moduleHtml.append(moduleContent);
            }

            return moduleHtml.toString();
        }

        return "Không tìm thấy tài liệu.";
    }

    // Phương thức để lấy tên hàm đúng dựa trên loại module
    private String getModuleFunction(String moduleType) {
        switch (moduleType) {
            case "assign":
                return "mod_assign_get_assignments"; // Lấy assignments
            case "resource":
                return "mod_resource_get_resources_by_courses"; // Lấy resources
            case "quiz":
                return "mod_quiz_get_quizzes_by_courses"; // Lấy quizzes
            // Thêm các loại module khác nếu cần
            default:
                return "unknown_module_type"; // Mặc định cho loại không xác định
        }
    }


    // bai thi
    public List<AttemptIDTO> getStudentAttempts(String studentId, String quizId, Double maxGrade, Integer numberOfQuestions) {
        String getAttemptsFunction = "mod_quiz_get_user_attempts";
        List<AttemptIDTO> attemptList = new ArrayList<>();

        try {
            String getAttemptsUrl = domainName + "/webservice/rest/server.php" +
                    "?wstoken=" + token +
                    "&wsfunction=" + getAttemptsFunction +
                    "&moodlewsrestformat=json" +
                    "&quizid=" + quizId +
                    "&userid=" + studentId;

            String attemptsResponse = restTemplate.getForObject(getAttemptsUrl, String.class);
            System.out.println("Request URL: " + getAttemptsUrl);
            System.out.println("Response: " + attemptsResponse);

            JSONObject responseJson = new JSONObject(attemptsResponse);
            JSONArray attemptsArray = responseJson.optJSONArray("attempts");

            if (attemptsArray != null && attemptsArray.length() > 0) {
                for (int i = 0; i < attemptsArray.length(); i++) {
                    JSONObject attempt = attemptsArray.getJSONObject(i);
                    Integer attemptId = attempt.getInt("id");
                    Double score = attempt.optDouble("sumgrades", 0.0);
                    Long timeStartLong = attempt.optLong("timestart", 0L);
                    Long timeFinishLong = attempt.optLong("timefinish", 0L);
                    String status = attempt.optString("state", "unknown");

                    // Chuyển đổi từ long sang LocalDateTime
                    LocalDateTime timeStart = LocalDateTime.ofEpochSecond(timeStartLong, 0, ZoneOffset.UTC);
                    LocalDateTime timeFinish = LocalDateTime.ofEpochSecond(timeFinishLong, 0, ZoneOffset.UTC);

                    // Tạo đối tượng AttemptIDTO với tất cả các tham số
                    AttemptIDTO attemptInfo = new AttemptIDTO(attemptId, score, maxGrade, numberOfQuestions, timeStart, timeFinish, status);

                    // Thêm vào danh sách
                    attemptList.add(attemptInfo);
                }
            } else {
                System.out.println("No attempts found for the student in this quiz.");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("Error parsing JSON response: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }

        return attemptList;
    }

    public QuizDetails getQuizDetails(String quizId, Integer moodleCourseId) {
        String getQuizDetailsFunction = "mod_quiz_get_quizzes_by_courses";
        Double maxGrade = null;
        Integer numberOfQuestions = null;

        try {
            String getQuizDetailsUrl = domainName + "/webservice/rest/server.php" +
                    "?wstoken=" + token +
                    "&wsfunction=" + getQuizDetailsFunction +
                    "&moodlewsrestformat=json" +
                    "&courseids[0]=" + moodleCourseId;  // Sử dụng courseId để lấy danh sách các quiz trong khóa học

            String quizDetailsResponse = restTemplate.getForObject(getQuizDetailsUrl, String.class);
            System.out.println("Request URL: " + getQuizDetailsUrl);
            System.out.println("Response: " + quizDetailsResponse);

            JSONObject responseJson = new JSONObject(quizDetailsResponse);
            JSONArray quizzesArray = responseJson.optJSONArray("quizzes");

            if (quizzesArray != null && quizzesArray.length() > 0) {
                for (int i = 0; i < quizzesArray.length(); i++) {
                    JSONObject quizDetails = quizzesArray.getJSONObject(i);

                    // Kiểm tra xem quizId có khớp với quiz hiện tại không
                    if (quizDetails.optString("id").equals(quizId)) {
                        maxGrade = quizDetails.optDouble("grade", 0.0); // Lấy maxGrade
                        numberOfQuestions = quizDetails.optInt("sumgrades", 0); // Lấy sumgrades làm số câu hỏi
                        System.out.println("Max Grade: " + maxGrade + ", Total Questions: " + numberOfQuestions);
                        break;  // Thoát vòng lặp sau khi tìm thấy quiz tương ứng
                    }
                }
            } else {
                System.out.println("No quizzes found for courseId: " + moodleCourseId);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }

        return new QuizDetails(maxGrade, numberOfQuestions); // Trả về chi tiết quiz
    }


    //bat dau 1 bai kiem tra moi
//    public String getAttemptId(String quizId, String userId) {
//        String apiFunction = "local_student_test_custom_start_attempt";
//        String attemptId = null;
//
//        try {
//            // Tạo URL với các tham số quizId và userId
//            String requestUrl = domainName + "/webservice/rest/server.php" +
//                    "?wstoken=" + token +
//                    "&wsfunction=" + apiFunction +
//                    "&moodlewsrestformat=json" +
//                    "&quizid=" + quizId +
//                    "&userid=" + userId;
//
//            // Gửi yêu cầu GET và nhận phản hồi từ API
//            String response = restTemplate.getForObject(requestUrl, String.class);
//            System.out.println("Request URL: " + requestUrl);
//            System.out.println("Response: " + response);
//
//            // Parse JSON để lấy attemptId từ response
//            JSONObject responseJson = new JSONObject(response);
//
//            // Kiểm tra nếu có lỗi trong response JSON
//            if (responseJson.has("exception")) {
//                String error = responseJson.optString("exception");
//                System.out.println("Error in response: " + error);
//                return null;
//            }
//
//            // Kiểm tra và lấy attempt từ response
//            attemptId = responseJson.optString("attempt");
//            if (attemptId == null || attemptId.isEmpty()) {
//                System.out.println("Attempt ID not found or is empty in response.");
//            } else {
//                System.out.println("Attempt ID retrieved: " + attemptId);
//            }
//
//        } catch (Exception e) {
//            // Log error chi tiết hơn
//            System.out.println("Error retrieving attempt ID: " + e.getMessage());
//            e.printStackTrace();
//        }
//
//        return attemptId;
//    }


    // Hàm tính điểm (grade) từ score và maxGrade
    private Double calculateGrade(Double score, Double maxGrade) {
        return (maxGrade != 0) ? (score / maxGrade) * 10 : 0.0; // Tính điểm trên thang 10
    }

    // thong bao trong khoa hoc
    public List<ForumDiscussionDTO> fetchForumContent(Integer instanceId) {
        // Hàm gọi API Moodle
        String getForumContentFunction = "mod_forum_get_forum_discussions";

        String apiUrl = domainName + "/webservice/rest/server.php" +
                "?wstoken=" + token +
                "&wsfunction=" + getForumContentFunction +
                "&moodlewsrestformat=json" +
                "&forumid=" + instanceId;

        RestTemplate restTemplate = new RestTemplate();
        try {
            String response = restTemplate.getForObject(apiUrl, String.class);
            System.out.println("API URL: " + apiUrl);
            System.out.println("Response: " + response);

            return processForumDiscussions(response); // Return the list of DTOs
        } catch (Exception e) {
            System.out.println("Error fetching forum content: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList(); // Return an empty list in case of error
        }
    }

    private List<ForumDiscussionDTO> processForumDiscussions(String jsonResponse) {
        List<ForumDiscussionDTO> discussions = new ArrayList<>();
        try {
            JSONObject responseJson = new JSONObject(jsonResponse);
            JSONArray discussionsArray = responseJson.optJSONArray("discussions");

            if (discussionsArray != null && discussionsArray.length() > 0) {
                for (int i = 0; i < discussionsArray.length(); i++) {
                    JSONObject discussion = discussionsArray.getJSONObject(i);
                    String subject = discussion.getString("subject");
                    String message = discussion.getString("message");
                    String userFullName = discussion.getString("userfullname"); // Get user full name

                    // Create DTO and add it to the list
                    discussions.add(new ForumDiscussionDTO(subject, message, userFullName));
                }
            } else {
                System.out.println("No discussions found for this forum.");
            }
        } catch (JSONException e) {
            System.out.println("Error parsing JSON response: " + e.getMessage());
            e.printStackTrace();
        }
        return discussions; // Return the list of DTOs
    }


    // Hàm để loại bỏ các ký tự như a., b., c. từ câu trả lời
    private String cleanResponse(String response) {
        return response.replaceAll("^[a-d]\\.", "").trim(); // Loại bỏ a., b., c., d. ở đầu và khoảng trắng thừa
    }

    public List<QuestionDetail> getAttemptDetails(Integer attemptId) {
        List<QuestionDetail> questionDetails = new ArrayList<>();
        try {
            String getAttemptReviewFunction = "mod_quiz_get_attempt_review";
            String getAttemptReviewUrl = domainName + "/webservice/rest/server.php" +
                    "?wstoken=" + token +
                    "&wsfunction=" + getAttemptReviewFunction +
                    "&moodlewsrestformat=json" +
                    "&attemptid=" + attemptId;

            String attemptReviewResponse = restTemplate.getForObject(getAttemptReviewUrl, String.class);
            JSONObject reviewJson = new JSONObject(attemptReviewResponse);
            JSONArray questionArray = reviewJson.getJSONArray("questions");

            for (int i = 0; i < questionArray.length(); i++) {
                JSONObject questionJson = questionArray.getJSONObject(i);

                // Khởi tạo QuestionDetail với số thứ tự câu hỏi
                QuestionDetail questionDetail = new QuestionDetail(i + 1, "", "", "", new ArrayList<>(), "", false);

                // Parse HTML từ câu hỏi
                String questionHtml = questionJson.optString("html", "");
                Document doc = Jsoup.parse(questionHtml);

                // Lấy nội dung câu hỏi
                String questionText = doc.select(".qtext").text();
                String correctResponse = cleanResponse(doc.select(".rightanswer").text().replace("The correct answer is: ", ""));

                // Lấy tất cả các phương án trả lời
                Elements answerElements = doc.select(".answer .r0, .answer .r1");
                List<String> allResponses = new ArrayList<>();
                String studentResponse = "";

                // Duyệt qua từng đáp án để thêm vào danh sách và kiểm tra câu trả lời của sinh viên
                for (Element answerElement : answerElements) {
                    String responseText = answerElement.text();
                    allResponses.add(cleanResponse(responseText));  // Clean trước khi lưu vào danh sách đáp án

                    // Kiểm tra nếu đây là câu trả lời sinh viên đã chọn
                    if (answerElement.select("input[checked=checked]").size() > 0) {
                        studentResponse = cleanResponse(responseText);  // Cắt ký tự a., b., c. trong câu trả lời sinh viên đã chọn
                    }
                }

                // So sánh câu trả lời của sinh viên với câu trả lời đúng sau khi đã xử lý
                boolean isCorrect = studentResponse.equalsIgnoreCase(correctResponse);

                // Đặt giá trị vào `questionDetail`
                questionDetail.setQuestionText(questionText);
                questionDetail.setStudentResponse(studentResponse);  // Đã được xử lý sạch
                questionDetail.setCorrectResponse(correctResponse);  // Đã được xử lý sạch
                questionDetail.setAllResponses(allResponses);  // Lưu danh sách đáp án đã được xử lý
                questionDetail.setCorrect(isCorrect);  // Đặt trạng thái đúng/sai

                System.out.println("Student Response: " + studentResponse);
                System.out.println("Correct Response: " + correctResponse);
                System.out.println("isCorrect: " + isCorrect);
                // Thêm câu hỏi vào danh sách
                questionDetails.add(questionDetail);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return questionDetails;
    }

    // Method to get all enrolled students from Moodle
    public List<StudentsCourseDTO> getEnrolledStudents(Integer moodleCourseId) {
        List<StudentsCourseDTO> students = new ArrayList<>();
        try {
            String getEnrolledUsersFunction = "core_enrol_get_enrolled_users";
            String getEnrolledUsersUrl = domainName + "/webservice/rest/server.php" +
                    "?wstoken=" + token +
                    "&wsfunction=" + getEnrolledUsersFunction +
                    "&moodlewsrestformat=json" +
                    "&courseid=" + moodleCourseId;

            // Log the URL for debugging
            System.out.println("Enrolled Users URL: " + getEnrolledUsersUrl);

            // Make the API request and log the response
            String enrolledUsersResponse = restTemplate.getForObject(getEnrolledUsersUrl, String.class);
            System.out.println("API Response: " + enrolledUsersResponse);

            // Parse the JSON response
            JSONArray usersArray = new JSONArray(enrolledUsersResponse);
            System.out.println("Number of users enrolled: " + usersArray.length());

            // Format for converting Unix timestamp to readable date
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject userJson = usersArray.getJSONObject(i);

                Integer userId = userJson.getInt("id");
                String fullName = userJson.getString("fullname");
                String firstName = userJson.getString("firstname");
                String lastName = userJson.getString("lastname");

                // Log user details
                System.out.println("User ID: " + userId + ", Full Name: " + fullName + ", First Name: " + firstName + ", Last Name: " + lastName);

                // Retrieve role shortname
                JSONArray rolesArray = userJson.getJSONArray("roles");
                String role = rolesArray.length() > 0 ? rolesArray.getJSONObject(0).getString("shortname") : "";
                System.out.println("Role: " + role);

                // Retrieve last access time and convert to readable date
                Long lastCourseAccess = userJson.optLong("lastcourseaccess", 0);
                String lastCourseAccessDate = lastCourseAccess > 0 ? sdf.format(new Date(lastCourseAccess * 1000L)) : "Never";
                System.out.println("Last Course Access: " + lastCourseAccessDate);

                // Create StudentsCourseDTO object with firstName and lastName
                StudentsCourseDTO student = new StudentsCourseDTO(userId, fullName, firstName, lastName, role, lastCourseAccess);
                students.add(student);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return students;
    }

    //diem so
    public GradesDTO getStudentGrades(Integer moodleCourseId, Integer userId) {
        GradesDTO gradesDTO = new GradesDTO();
        try {
            String getGradesFunction = "gradereport_user_get_grades_table";
            String getGradesUrl = domainName + "/webservice/rest/server.php" +
                    "?wstoken=" + token +
                    "&wsfunction=" + getGradesFunction +
                    "&moodlewsrestformat=json" +
                    "&courseid=" + moodleCourseId +
                    "&userid=" + userId;

            String gradesResponse = restTemplate.getForObject(getGradesUrl, String.class);
            System.out.println("Grades API Response: " + gradesResponse);

            JSONObject responseJson = new JSONObject(gradesResponse);
            JSONArray tablesArray = responseJson.getJSONArray("tables");

            if (tablesArray.length() > 0) {
                JSONObject tableJson = tablesArray.getJSONObject(0);
                JSONArray tableData = tableJson.getJSONArray("tabledata");

                List<GradeItemDTO> gradeItems = new ArrayList<>();
                for (int i = 0; i < tableData.length(); i++) {
                    JSONObject item = tableData.getJSONObject(i);
                    String htmlContent = item.optString("itemname", "");

                    if (!htmlContent.isEmpty()) {
                        Document doc = Jsoup.parse(htmlContent);
                        Elements spans = doc.select("span");

                        String itemName = spans.size() > 0 ? spans.get(spans.size() - 1).text() : "N/A";
                        itemName = itemName.replaceAll("<[^>]*>", "")
                                .replaceAll("\\\\n", "")
                                .replaceAll("\\\\", "")
                                .replaceAll("\\s+", " ")
                                .replaceAll("[\"{}]", "")
                                .trim();

                        // Check if it's the course total and ensure no prefixes like "Test" or "Quiz"
                        if (itemName.contains("Course total")) {
                            itemName = "Aggregation Course total";
                        } else {
                            // Format itemName for line breaks if starting with "Quiz" or "Test"
                            if (itemName.startsWith("Quiz")) {
                                itemName = "Quiz\n" + itemName.substring(4).trim();
                            } else if (itemName.startsWith("Test")) {
                                itemName = "Test\n" + itemName.substring(4).trim();
                            }
                        }

                        System.out.println("Final itemName: " + itemName);

                        // Set the first item as course name
                        if (i == 0) {
                            gradesDTO.setCourseName(itemName);
                            continue;
                        }

                        Double grade = item.optJSONObject("grade") != null ?
                                item.getJSONObject("grade").optDouble("content", 0.0) : 0.0;

                        Double maxGrade = item.optJSONObject("grademax") != null ?
                                item.getJSONObject("grademax").optDouble("content", 0.0) : 0.0;

                        String percentage = item.optJSONObject("percentage") != null ?
                                item.getJSONObject("percentage").optString("content", "N/A") : "N/A";

                        String weight = item.optJSONObject("weight") != null ?
                                item.getJSONObject("weight").optString("content", "N/A") : "N/A";

                        String range = maxGrade != 0 ? "0-" + maxGrade : "N/A";

                        String feedback = item.optJSONObject("feedback") != null ?
                                item.getJSONObject("feedback").optString("content", "").replace("&nbsp;", "").trim() : "";

                        String contributionToCourseTotal = item.optJSONObject("contributiontocoursetotal") != null ?
                                item.getJSONObject("contributiontocoursetotal").optString("content", "N/A") : "N/A";

                        // Add the grade item to the list
                        GradeItemDTO gradeItemDTO = new GradeItemDTO(
                                itemName, grade, maxGrade, percentage, weight, range, feedback, contributionToCourseTotal
                        );
                        gradeItems.add(gradeItemDTO);
                    }
                }

                gradesDTO.setUserId(userId);
                gradesDTO.setMoodleCourseId(moodleCourseId);
                gradesDTO.setGradeItems(gradeItems);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return gradesDTO;
    }

    // Lưu khóa học vừa truy cập vào cơ sở dữ liệu
    public void saveAccessedCourse(Integer userId, Integer courseId, String courseName, String categoryName, String instructorName) {
        // Kiểm tra xem khóa học đã được người dùng truy cập chưa
        Optional<RecentlyAccessedCoursesEntity> existingCourseOpt = recentlyAccessedCoursesRepository
                .findByUserIdAndCourseId(userId, courseId);

        // Nếu khóa học đã tồn tại trong cơ sở dữ liệu
        if (existingCourseOpt.isPresent()) {
            // Cập nhật timestamp và các chi tiết khác cho bản ghi hiện có
            RecentlyAccessedCoursesEntity existingCourse = existingCourseOpt.get();
            existingCourse.setAccessedAt(LocalDateTime.now());

            // Chỉ cập nhật khi các giá trị không phải null
            existingCourse.setCourseName(courseName != null ? courseName : existingCourse.getCourseName());
            existingCourse.setCategoryName(categoryName != null ? categoryName : existingCourse.getCategoryName());
            existingCourse.setInstructorName(instructorName != null ? instructorName : "Chưa có giảng viên");

            recentlyAccessedCoursesRepository.save(existingCourse);
            System.out.println("Updated accessed time for course with userId: " + userId + " and courseId: " + courseId);
        } else {
            // Tạo một bản ghi mới nếu chưa có bản ghi nào tồn tại
            RecentlyAccessedCoursesEntity accessedCourse = new RecentlyAccessedCoursesEntity(userId, courseId);
            accessedCourse.setAccessedAt(LocalDateTime.now());
            accessedCourse.setCourseName(courseName);
            accessedCourse.setCategoryName(categoryName);
            accessedCourse.setInstructorName(instructorName != null ? instructorName : "Chưa có giảng viên");

            recentlyAccessedCoursesRepository.save(accessedCourse);
            System.out.println("Saved accessed course with userId: " + userId + " and courseId: " + courseId);
        }
    }

    // Lấy danh sách các khóa học vừa truy cập của một người dùng
    public List<RecentlyAccessedCourseDTO> getRecentlyAccessedCourses(Integer userId) {
        List<RecentlyAccessedCoursesEntity> courses = recentlyAccessedCoursesRepository.findByUserIdOrderByAccessedAtDesc(userId);
        System.out.println("Recently accessed courses for user " + userId + ": " + courses);

        // Chuyển đổi các entities thành DTO và thêm thông tin chi tiết
        return courses.stream()
                .map(course -> {
                    RecentlyAccessedCourseDTO dto = course.toDTO();
                    // Bạn có thể bổ sung thêm các thông tin chi tiết vào DTO nếu cần
                    // Ví dụ: thiết lập tên khóa học, danh mục, giảng viên từ entity
                    dto.setCourseName(course.getCourseName());
                    dto.setCategoryName(course.getCategoryName());
                    dto.setInstructorName(course.getInstructorName());
                    return dto;
                })
                .collect(Collectors.toList());
    }


}
