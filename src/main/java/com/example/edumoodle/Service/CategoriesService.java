package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.CategoriesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoriesService {

    @Value("${moodle.token}")
    private String token;

    @Value("${moodle.domainName}")
    private String domainName;

    private static final Logger logger = LoggerFactory.getLogger(CategoriesService.class);

    public Map<Integer, List<CategoriesDTO>> getCategoriesGroupedByParent() {
        String apiMoodleFunc = "core_course_get_categories";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json";

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<CategoriesDTO[]> response;
        try {
            response = restTemplate.getForEntity(url, CategoriesDTO[].class);
        } catch (Exception e) {
            logger.error("Error occurred while fetching categories from Moodle API: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to fetch categories from Moodle API", e);
        }

        CategoriesDTO[] categories = response.getBody();

        if (categories == null || categories.length == 0) {
            logger.warn("No categories returned from Moodle API");
            return new HashMap<>();
        }

        logger.info("Fetched {} categories from Moodle API", categories.length);

        // Nhóm các danh mục theo parent
        Map<Integer, List<CategoriesDTO>> categoryMap = new HashMap<>();

        for (CategoriesDTO category : categories) {
            categoryMap.putIfAbsent(category.getParent(), new ArrayList<>());
            categoryMap.get(category.getParent()).add(category);
        }

        return categoryMap;
    }
//tính tổng số khóa học cho danh mục cha
    public Map<Integer, Integer> getTotalCoursesByParent() {
        Map<Integer, List<CategoriesDTO>> categoriesGroupedByParent = getCategoriesGroupedByParent();
        Map<Integer, Integer> totalCoursesByParent = new HashMap<>();

        for (Map.Entry<Integer, List<CategoriesDTO>> entry : categoriesGroupedByParent.entrySet()) {
            int parentId = entry.getKey();
            List<CategoriesDTO> subCategories = entry.getValue();
            int totalCourses = 0;

            for (CategoriesDTO subCategory : subCategories) {
                totalCourses += subCategory.getCoursecount(); // Số khóa học của danh mục con
            }

            totalCoursesByParent.put(parentId, totalCourses);
        }

        return totalCoursesByParent;
    }
}
