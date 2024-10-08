package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.CategoriesDTO;
import com.example.edumoodle.DTO.CategoryHierarchyDTO;
import com.example.edumoodle.DTO.CoursesDTO;
import com.example.edumoodle.Model.CategoriesEntity;
import com.example.edumoodle.Repository.CategoriesRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoriesService {

    @Value("${moodle.token}")
    private String token;

    @Value("${moodle.domainName}")
    private String domainName;

    @Autowired
    private CategoriesRepository categoriesRepository;

    private static final Logger logger = LoggerFactory.getLogger(CategoriesService.class);

    //lấy all categories
    public List<CategoriesDTO> getAllCategory() {
        String apiMoodleFunc = "core_course_get_categories";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json";

        RestTemplate restTemplate = new RestTemplate();
        CategoriesDTO[] categories = restTemplate.getForObject(url, CategoriesDTO[].class);

        assert categories != null;
        return List.of(categories);
    }

    //lấy ra danh mục bằng fullname
    public List<CategoriesDTO> getSearchCategory(String fullname) {
        List<CategoriesDTO> categories = getAllCategory();
        return categories.stream()
                .filter(category -> StringEscapeUtils.unescapeHtml4(category.getName().toLowerCase()).contains(fullname.toLowerCase()))
                .collect(Collectors.toList());
    }

    //lưu tất cả categories lấy đc lưu vào csdl web + update again
    public void saveCategories(List<CategoriesDTO> categories) {
        // Lấy tất cả danh mục hiện có từ cơ sở dữ liệu web
        List<CategoriesEntity> existingCategories = categoriesRepository.findAll();

        // Tạo một tập hợp các ID của danh mục từ Moodle để kiểm tra
        Set<Integer> moodleIds = categories.stream()
                .map(CategoriesDTO::getId)
                .collect(Collectors.toSet());

        // Cập nhật hoặc lưu các danh mục từ Moodle
        for (CategoriesDTO dto : categories) {
            Optional<CategoriesEntity> existingCategory = existingCategories.stream()
                    .filter(c -> c.getMoodleId().equals(dto.getId()))
                    .findFirst();

            if (existingCategory.isPresent()) {
                // Nếu danh mục đã tồn tại, kiểm tra và cập nhật nếu cần thiết
                CategoriesEntity category = existingCategory.get();
                boolean updated = false;

                if (!category.getName().equals(dto.getName())) {
                    category.setName(dto.getName());
                    updated = true;
                }
                if (!category.getParent().equals(dto.getParent())) {
                    category.setParent(dto.getParent());
                    updated = true;
                }
                if (category.getCoursecount() != dto.getCoursecount()) {
                    category.setCoursecount(dto.getCoursecount());
                    updated = true;
                }
                if (!category.getDescription().equals(dto.getDescription())) {
                    category.setDescription(dto.getDescription());
                    updated = true;
                }

                // Lưu nếu có thay đổi
                if (updated) {
                    categoriesRepository.save(category);
                }
            } else {
                // Nếu danh mục không tồn tại, tạo mới
                CategoriesEntity newCategory = new CategoriesEntity();
                newCategory.setMoodleId(dto.getId());
                newCategory.setName(dto.getName());
                newCategory.setParent(dto.getParent());
                newCategory.setCoursecount(dto.getCoursecount());
                newCategory.setDescription(dto.getDescription());
                categoriesRepository.save(newCategory);
            }
        }

        // Xóa các danh mục không còn tồn tại trên Moodle
        for (CategoriesEntity existingCategory : existingCategories) {
            if (!moodleIds.contains(existingCategory.getMoodleId())) {
                categoriesRepository.delete(existingCategory);
            }
        }
    }

    //tạo danh mục
    public void createCategory(CategoriesDTO categoriesDTO) {
        String apiMoodleFunc = "core_course_create_categories";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json"
                + "&categories[0][name]=" + categoriesDTO.getName()
                + "&categories[0][parent]=" + categoriesDTO.getParent(); // Nếu có ID của danh mục gốc

        // Gửi yêu cầu tạo danh mục đến Moodle
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            // Xử lý phản hồi từ Moodle
            System.out.println("Response: " + response.getBody());
        } catch (Exception e) {
            // Xử lý ngoại lệ
            e.printStackTrace();
        }
    }

    //lấy ra nhóm danh mục cha và danh mục cha có con
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

//        logger.info("Fetched {} categories from Moodle API", categories.length);

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

    //lấy danh sách cha - con dạng "cha / con" trong ô select
    public List<CategoryHierarchyDTO> getParentChildCategories() {
        Map<Integer, List<CategoriesDTO>> categoriesGroupedByParent = getCategoriesGroupedByParent();
        List<CategoryHierarchyDTO> categoryHierarchy = new ArrayList<>();
        Set<Integer> parentIdsWithChildren = new HashSet<>();

        // Lưu trữ tất cả các danh mục vào tập hợp
        Set<CategoriesDTO> allCategories = new HashSet<>();
        for (List<CategoriesDTO> categories : categoriesGroupedByParent.values()) {
            for (CategoriesDTO category : categories) {
                if (category.getParent() == 0) {  // Chỉ thêm những danh mục có parentId khác 0
                    allCategories.add(category);
                }
            }
        }

        // Duyệt qua các danh mục cha
        for (Map.Entry<Integer, List<CategoriesDTO>> entry : categoriesGroupedByParent.entrySet()) {
            int parentId = entry.getKey();
            List<CategoriesDTO> subCategories = entry.getValue();

            // Tìm danh mục cha
            CategoriesDTO parentCategory = findParentCategory(parentId, categoriesGroupedByParent);
            if (parentCategory != null) {
                String parentCategoryName = parentCategory.getName();
                Integer parentCategoryId = parentCategory.getId();

                // Nếu danh mục cha có danh mục con
                if (subCategories != null && !subCategories.isEmpty()) {
                    parentIdsWithChildren.add(parentId); // Đánh dấu danh mục cha có con
                    categoryHierarchy.add(new CategoryHierarchyDTO(parentCategoryId, parentCategoryName)); // Thêm danh mục cha

                    for (CategoriesDTO subCategory : subCategories) {
                        // Thêm cấu trúc "cha / con" vào danh sách
                        String subCategoryName = parentCategoryName + " / " + subCategory.getName();
                        categoryHierarchy.add(new CategoryHierarchyDTO(subCategory.getId(), subCategoryName));
                    }
                }
            }
        }

        // Xử lý các danh mục không có con
        for (CategoriesDTO category : allCategories) {
            if (!parentIdsWithChildren.contains(category.getId()) &&
                    categoryHierarchy.stream().noneMatch(c -> c.getName().equals(category.getName()))) {
                categoryHierarchy.add(new CategoryHierarchyDTO(category.getId(), category.getName()));
            }
        }

        return categoryHierarchy;
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
