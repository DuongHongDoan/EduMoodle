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

        // Duyệt qua tất cả các danh mục
        for (Map.Entry<Integer, List<CategoriesDTO>> entry : categoriesGroupedByParent.entrySet()) {
            int parentId = entry.getKey();
            List<CategoriesDTO> subCategories = entry.getValue();
            int totalCourses = 0;

            // Tính số khóa học của các danh mục con
            if (subCategories != null && !subCategories.isEmpty()) {
                for (CategoriesDTO subCategory : subCategories) {
                    totalCourses += subCategory.getCoursecount(); // Số khóa học của danh mục con
                }
            }

            // Cộng khóa học của chính danh mục cha
            CategoriesDTO parentCategory = findParentCategory(parentId, categoriesGroupedByParent);
            if (parentCategory != null) {
                totalCourses += parentCategory.getCoursecount(); // Cộng khóa học của chính danh mục cha
            }

            // Đưa tổng số khóa học vào Map
            totalCoursesByParent.put(parentId, totalCourses);
        }

        // Xử lý các danh mục không có con nhưng có khóa học
        for (List<CategoriesDTO> categories : categoriesGroupedByParent.values()) {
            for (CategoriesDTO category : categories) {
                // Nếu danh mục không phải là cha của bất kỳ danh mục con nào
                if (!totalCoursesByParent.containsKey(category.getId())) {
                    totalCoursesByParent.put(category.getId(), category.getCoursecount());
                }
            }
        }

        return totalCoursesByParent;
    }

    // Phương thức tìm kiếm danh mục cha theo ID
    private CategoriesDTO findParentCategory(int parentId, Map<Integer, List<CategoriesDTO>> categoriesGroupedByParent) {
        for (List<CategoriesDTO> categories : categoriesGroupedByParent.values()) {
            for (CategoriesDTO category : categories) {
                if (category.getId() == parentId) {
                    return category;
                }
            }
        }
        return null;
    }


}
