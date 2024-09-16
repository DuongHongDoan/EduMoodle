package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.CategoriesDTO;
import com.example.edumoodle.DTO.CoursesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CoursesService {

    @Value("${moodle.token}")
    private String token;

    @Value("${moodle.domainName}")
    private String domainName;

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

    // Lấy tất cả các danh mục
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

    public List<CategoriesDTO> getAllCategories() {
        String apiMoodleFunc = "core_course_get_categories";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json";

        RestTemplate restTemplate = new RestTemplate();
        CategoriesDTO[] categoryArray = restTemplate.getForObject(url, CategoriesDTO[].class);
        assert categoryArray != null;
        return Arrays.asList(categoryArray);
    }

//    public List<CoursesDTO> getCoursesByCategory(int categoryId) {
//        List<CoursesDTO> allCourses = getAllCourses();
//        return allCourses.stream()
//                .filter(course -> course.getCategoryid() == categoryId)
//                .collect(Collectors.toList());
//    }

    public List<CoursesDTO> getCoursesByParentCategory(int parentCategoryId) {
        List<CategoriesDTO> allCategories = getAllCategories();
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
}
