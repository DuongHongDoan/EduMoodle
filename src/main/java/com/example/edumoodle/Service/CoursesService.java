package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.SectionsDTO;
import com.example.edumoodle.DTO.CategoriesDTO;
import com.example.edumoodle.DTO.CoursesDTO;
import com.example.edumoodle.Model.CategoriesEntity;
import com.example.edumoodle.Model.CoursesEntity;
import com.example.edumoodle.Repository.CategoriesRepository;
import com.example.edumoodle.Repository.CoursesRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CoursesService {

    @Value("${moodle.token}")
    private String token;

    @Value("${moodle.domainName}")
    private String domainName;

    @Autowired
    private CoursesRepository coursesRepository;
    @Autowired
    private CategoriesRepository categoriesRepository;
    @Autowired
    private CategoriesService categoriesService;

    //lấy tất cả khóa học
    public List<CoursesDTO> getAllCourses() {
        String apiMoodleFunc = "core_course_get_courses";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json";

        RestTemplate restTemplate = new RestTemplate();
        CoursesDTO[] courseArray = restTemplate.getForObject(url, CoursesDTO[].class);

        // Lọc bỏ những course có categoryid = 0
        assert courseArray != null;

        return Arrays.stream(courseArray)
                .filter(course -> course.getCategoryid() != null && course.getCategoryid() != 0)
                .collect(Collectors.toList());
    }

    //lấy thông tin của 1 khóa học
    public CoursesDTO getCourseDetail(Integer courseId) {
        String apiMoodleFunc = "core_course_get_courses";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json"
                + "&options[ids][0]=" + courseId;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<CoursesDTO[]> response = restTemplate.getForEntity(url, CoursesDTO[].class);
        CoursesDTO[] courses = response.getBody();

        assert courses != null;
        return courses.length > 0 ? courses[0] : null;
    }

    // Lấy tất cả các danh mục với mỗi khóa học tương ứng với danh mục (như join 2 bảng category với course trên moodle)
    public Map<Integer, String> getMapCategories() {
        String apiMoodleFunc = "core_course_get_categories";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json";

        RestTemplate restTemplate = new RestTemplate();
        CategoriesDTO[] categoryArray = restTemplate.getForObject(url, CategoriesDTO[].class);

        // Chuyển đổi danh sách danh mục thành Map<id, name> để dễ dàng ánh xạ categoryID bên course sang categoryName
        assert categoryArray != null;
        return Arrays.stream(categoryArray)
                .collect(Collectors.toMap(CategoriesDTO::getId, CategoriesDTO::getName));
    }

    //lấy danh sách khóa học theo danh mục cha/con
    public List<CoursesDTO> getCoursesByParentCategory(int parentCategoryId) {
        List<CategoriesDTO> allCategories = categoriesService.getAllCategory();
        List<Integer> categoryIds = allCategories.stream()
                .filter(category -> category.getParent() == parentCategoryId)
                .map(CategoriesDTO::getId)
                .collect(Collectors.toList());
        categoryIds.add(parentCategoryId); // Thêm danh mục cha vào danh sách

        List<CoursesDTO> allCourses = getAllCourses();
        return allCourses.stream()
                .filter(course -> categoryIds.contains(course.getCategoryid()))
                .collect(Collectors.toList());
    }

    //lấy nội dung bên trong của từng khóa học
    public List<SectionsDTO> getCourseContent(Integer courseId) {
        String apiMoodleFunc = "core_course_get_contents";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json"
                + "&courseid=" + courseId;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<SectionsDTO[]> response = restTemplate.getForEntity(url, SectionsDTO[].class);
        SectionsDTO[] sections = response.getBody();

        assert sections != null;
        return Arrays.asList(sections);
    }

    //xử lý dữ liệu từ form ô tìm kiếm khóa học
    public List<CoursesDTO> getSearchCourses(String keyword) {
        List<CoursesDTO> courses = getAllCourses();

        return courses.stream()
                .filter(course -> course.getFullname().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    //tạo khóa hoc
    public String createCourse(CoursesDTO coursesDTO) {
        // Kiểm tra nếu categoryId được chọn từ Moodle thì sử dụng trực tiếp categoryId đó
        Integer moodleCategoryId = coursesDTO.getCategoryid(); // Category lấy từ Moodle có thể truyền trực tiếp

        String functionName = "core_course_create_courses";

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("courses[0][fullname]", coursesDTO.getFullname());
        parameters.add("courses[0][shortname]", coursesDTO.getShortname());
        parameters.add("courses[0][categoryid]", String.valueOf(moodleCategoryId));
        parameters.add("courses[0][summary]", coursesDTO.getSummary());
        parameters.add("moodlewsrestformat", "json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(parameters, headers);

        String serverUrl = domainName + "/webservice/rest/server.php" + "?wstoken=" + token + "&wsfunction=" + functionName;

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.postForObject(serverUrl, request, String.class);

        System.out.println(serverUrl);
        System.out.println(response);

        return response;
    }

    public Integer extractMoodleCourseId(String moodleResponse) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(moodleResponse);
            return root.path(0).path("id").asInt();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //đồng bộ csdl course web với moodle
    public void synchronizeCourses(List<CoursesDTO> moodleCourses) {
        // Lấy danh sách khóa học hiện có từ cơ sở dữ liệu
        List<CoursesEntity> existingCourses = coursesRepository.findAll();
        Set<Integer> existingCourseIds = existingCourses.stream()
                .map(CoursesEntity::getMoodleId)
                .collect(Collectors.toSet());


        // Lưu trữ ID khóa học từ Moodle
        Set<Integer> moodleCourseIds = moodleCourses.stream()
                .map(CoursesDTO::getId)
                .collect(Collectors.toSet());

        for (CoursesDTO moodleCourse : moodleCourses) {
            System.out.println("Processing Moodle course: " + moodleCourse.getFullname());
            CategoriesEntity category = categoriesRepository.findByMoodleId(moodleCourse.getCategoryid());

            if (!existingCourseIds.contains(moodleCourse.getId())) {
                // Nếu khóa học không tồn tại, thêm vào cơ sở dữ liệu
                CoursesEntity newCourse = new CoursesEntity();
                newCourse.setFullname(moodleCourse.getFullname());
                newCourse.setShortname(moodleCourse.getShortname());
                newCourse.setSummary(moodleCourse.getSummary());
                newCourse.setMoodleId(moodleCourse.getId());
                newCourse.setCategoriesEntity(category);
                coursesRepository.save(newCourse);
            } else {
                // Nếu khóa học đã tồn tại, kiểm tra và cập nhật thông tin nếu cần
                CoursesEntity existingCourse = existingCourses.stream()
                        .filter(course -> course.getMoodleId().equals(moodleCourse.getId()))
                        .findFirst()
                        .orElse(null);

                if (existingCourse != null) {
                    // Cập nhật thông tin khóa học nếu có thay đổi
                    existingCourse.setFullname(moodleCourse.getFullname());
                    existingCourse.setShortname(moodleCourse.getShortname());
                    existingCourse.setSummary(moodleCourse.getSummary());
                    existingCourse.setMoodleId(moodleCourse.getId());
                    existingCourse.setCategoriesEntity(category);
                    coursesRepository.save(existingCourse);
                }
            }
        }

        // Xóa các khóa học không còn tồn tại trên Moodle
        for (CoursesEntity existingCourse : existingCourses) {
            if (!moodleCourseIds.contains(existingCourse.getMoodleId())) {
                coursesRepository.delete(existingCourse);
            }

        }
    }

    //cập nhật khóa học
    public boolean updateMoodleCourse(CoursesDTO coursesDto) {
        String functionName = "core_course_update_courses";

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("courses[0][id]", String.valueOf(coursesDto.getId()));
        parameters.add("courses[0][categoryid]", String.valueOf(coursesDto.getCategoryid()));
        parameters.add("courses[0][fullname]", coursesDto.getFullname());
        parameters.add("courses[0][shortname]", coursesDto.getShortname()); // Chuyển đổi Integer thành String nếu cần
        parameters.add("courses[0][summary]", coursesDto.getSummary());
        parameters.add("moodlewsrestformat", "json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(parameters, headers);

        String serverUrl = domainName + "/webservice/rest/server.php" + "?wstoken=" + token + "&wsfunction=" + functionName;

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.postForObject(serverUrl, request, String.class);

        System.out.println("Moodle Response: " + response);
        System.out.println(serverUrl);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            // Kiểm tra phản hồi từ Moodle để xác định xem việc cập nhật có thành công hay không
            if (rootNode.has("warnings")) {
                for (JsonNode warning : rootNode.get("warnings")) {
                    System.out.println("Warning: " + warning.get("message").asText());
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //xóa khóa học trên csdl web
    public boolean deleteCourseFromDatabase(int courseId) {
        try {
            if (coursesRepository.existsById(courseId)) {
                coursesRepository.deleteById(courseId);
                return true;
            } else {
                System.out.println("Course with ID " + courseId + " does not exist in the database.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //xóa khóa học trên moodle
    public boolean deleteCourseFromMoodle(int courseId) {

        String functionName = "core_course_delete_courses";

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("moodlewsrestformat", "json");
        parameters.add("courseids[0]", String.valueOf(courseId));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(parameters, headers);
        String serverUrl = domainName + "/webservice/rest/server.php" + "?wstoken=" + token + "&wsfunction=" + functionName;

        RestTemplate restTemplate = new RestTemplate();
        try {
            String response = restTemplate.postForObject(serverUrl, request, String.class);
            System.out.println("Moodle Response: " + response); // Kiểm tra phản hồi từ Moodle

            return true;  // Hoặc điều chỉnh kiểm tra phản hồi cho phù hợp
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
