package com.example.edumoodle.Service;


import com.example.edumoodle.DTO.CoursesDTO;
import com.example.edumoodle.Repository.CategoriesRepository;
import com.example.edumoodle.Repository.CoursesRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

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


    public List<CoursesDTO> getUserCourses(String username) {
        List<CoursesDTO> userCourses = new ArrayList<>(); // Danh sách khóa học của người dùng
        String getStudentsFunction = "core_user_get_users";
        String getUserCoursesFunction = "core_enrol_get_users_courses";
        String getEnrolledUsersFunction = "core_enrol_get_enrolled_users"; // Lấy thông tin giảng viên

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

        // Đảm bảo user ID hợp lệ
        if (userId == 0) {
            return userCourses; // Trả về danh sách trống nếu không tìm thấy user ID
        }

        // URL API để lấy khóa học của người dùng
        String getUserCoursesUrl = domainName + "/webservice/rest/server.php" +
                "?wstoken=" + token +
                "&wsfunction=" + getUserCoursesFunction +
                "&moodlewsrestformat=json" +
                "&userid=" + userId;

        String coursesResponse = restTemplate.getForObject(getUserCoursesUrl, String.class);

        // Phân tích dữ liệu khóa học từ phản hồi
        try {
            JSONArray coursesArray = new JSONArray(coursesResponse);
            for (int i = 0; i < coursesArray.length(); i++) {
                JSONObject course = coursesArray.getJSONObject(i);
                int courseMoodleId = course.getInt("id");  // Lấy courseMoodleId
                String courseName = course.getString("fullname");

                // Lấy thông tin category và categoryName nếu có
                int category = course.has("category") ? course.getInt("category") : 0; // Mặc định là 0 nếu không có
                String categoryName;

                // Kiểm tra nếu categoryName không có trong phản hồi thì gọi API để lấy
                if (!course.has("categoryname")) {
                    categoryName = getCategoryName(category); // Gọi hàm để lấy tên category
                } else {
                    categoryName = course.getString("categoryname");
                }

                // Gọi API để lấy thông tin giảng viên của khóa học
                String getEnrolledUsersUrl = domainName + "/webservice/rest/server.php" +
                        "?wstoken=" + token +
                        "&wsfunction=" + getEnrolledUsersFunction +
                        "&moodlewsrestformat=json" +
                        "&courseid=" + courseMoodleId;

                String enrolledUsersResponse = restTemplate.getForObject(getEnrolledUsersUrl, String.class);

                System.out.println(getEnrolledUsersUrl);
                System.out.println("Enrolled Users Response: " + enrolledUsersResponse);
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

                System.out.println("Teacher: " + teacherName); // In ra tên giảng viên để kiểm tra
                // Tạo đối tượng CoursesDto và thêm vào danh sách
                CoursesDTO coursesDto = new CoursesDTO();
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

        return userCourses; // Trả về danh sách khóa học của người dùng
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
}
