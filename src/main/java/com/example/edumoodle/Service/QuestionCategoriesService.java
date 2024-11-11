package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.QuestionCategoriesResponseDTO;
import com.example.edumoodle.DTO.QuestionCategoriesDTO;
import com.example.edumoodle.DTO.QuestionCategoriesResponseDTO;
import com.example.edumoodle.Model.QuestionCategoriesEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import com.example.edumoodle.Repository.QuestionCategoriesRepository; // Thay thế với đường dẫn đúng

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionCategoriesService {

        private static final Logger logger = LoggerFactory.getLogger(QuestionCategoriesService.class);

        @Value("${moodle.token}")
        private String token;

        @Value("${moodle.domainName}")
        private String domainName;

        @Autowired
        private RestTemplate restTemplate;

    @Autowired
    private QuestionCategoriesRepository QuestionCategoriesRepository;

    //    lấy danh mục cu hỏi theo cha-con

    public List<QuestionCategoriesDTO> getQuestionCategories(int courseId) {
        String apiMoodleFunc = "local_question_get_categories";
        String url = String.format("%s/webservice/rest/server.php?wstoken=%s&wsfunction=%s&moodlewsrestformat=json&courseId=%d",
                    domainName, token, apiMoodleFunc, courseId);

        logger.info("Fetching question categories from URL: {}", url);

        try {
           // Gửi yêu cầu đến API và nhận kết quả
            ResponseEntity<QuestionCategoriesResponseDTO> response = restTemplate.getForEntity(url, QuestionCategoriesResponseDTO.class);
            logger.info("Response Status: {}", response.getStatusCode());

            // Kiểm tra nếu phản hồi không thành công
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                logger.error("Không có dữ liệu hoặc yêu cầu thất bại.");
                return Collections.emptyList();
            }

            // Kiểm tra trạng thái phản hồi
            if (!"success".equalsIgnoreCase(response.getBody().getStatus())) {
                logger.error("API trả về trạng thái không thành công: {}", response.getBody().getStatus());
                return Collections.emptyList();
            }

            // Trả về danh sách các danh mục câu hỏi
            List<QuestionCategoriesDTO> categories = response.getBody().getCategories();
            if (categories == null) {
                logger.error("Trường 'categories' là null.");
                    return Collections.emptyList();
            }
            logger.info("Fetched {} question categories.", categories.size());
            return buildCategoryTree(categories);
        } catch (Exception e) {
            // Xử lý lỗi ngoại lệ nếu xảy ra
            logger.error("Lỗi khi gọi API Moodle: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    //    danh sách cha - con
    public List<QuestionCategoriesDTO> buildCategoryTree(List<QuestionCategoriesDTO> categories) {
        Map<Integer, QuestionCategoriesDTO> categoryMap = categories.stream()
                .collect(Collectors.toMap(QuestionCategoriesDTO::getId, category -> category));

        List<QuestionCategoriesDTO> rootCategories = new ArrayList<>();

        for (QuestionCategoriesDTO category : categories) {
            if (category.getParent() == 0) {
                rootCategories.add(category);  // Thêm danh mục gốc
            } else {
                QuestionCategoriesDTO parent = categoryMap.get(category.getParent());
                if (parent != null) {
                    // Thêm category vào danh sách children của parent
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(category);
                }
            }
        }

        return rootCategories;
    }

    //  Lấy tất cả danh mục
    public List<QuestionCategoriesDTO> getAllQuestionCategories(int courseId) {
        String apiMoodleFunc = "local_question_get_categories";
        String url = String.format("%s/webservice/rest/server.php?wstoken=%s&wsfunction=%s&moodlewsrestformat=json&courseId=%d",
                domainName, token, apiMoodleFunc, courseId);

        logger.info("Fetching question categories from URL: {}", url);

        try {
            // Gửi yêu cầu đến API và nhận kết quả
            ResponseEntity<QuestionCategoriesResponseDTO> response = restTemplate.getForEntity(url, QuestionCategoriesResponseDTO.class);
            logger.info("Response Status: {}", response.getStatusCode());

            // Kiểm tra nếu phản hồi không thành công
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                logger.error("Không có dữ liệu hoặc yêu cầu thất bại.");
                return Collections.emptyList();
            }

            // Kiểm tra trạng thái phản hồi
            if (!"success".equalsIgnoreCase(response.getBody().getStatus())) {
                logger.error("API trả về trạng thái không thành công: {}", response.getBody().getStatus());
                return Collections.emptyList();
            }

            // Trả về danh sách các danh mục câu hỏi
            List<QuestionCategoriesDTO> categories = response.getBody().getCategories();
            if (categories == null) {
                logger.error("Trường 'categories' là null.");
                return Collections.emptyList();
            }
            logger.info("Fetched {} question categories.", categories.size());
               return categories;

        } catch (Exception e) {
            // Xử lý lỗi ngoại lệ nếu xảy ra
            logger.error("Lỗi khi gọi API Moodle: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // Thêm danh mục câu hỏi vào Moodle

    public String addCategory(String name, String info, int parentId, int courseId) {
        String apiMoodleFunc = "local_question_add_categories";
        String url = UriComponentsBuilder.fromHttpUrl(domainName + "/webservice/rest/server.php")
                .queryParam("wstoken", token)
                .queryParam("wsfunction", apiMoodleFunc)
                .queryParam("moodlewsrestformat", "json")
                .toUriString();

        // Tạo body yêu cầu dưới dạng MultiValueMap
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("name", name);
        requestBody.add("info", info);
        requestBody.add("parent", String.valueOf(parentId));
        requestBody.add("courseid", String.valueOf(courseId));

        // Thiết lập Header (application/x-www-form-urlencoded)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Tạo HttpEntity với body và header
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Gửi yêu cầu HTTP POST và nhận phản hồi
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // Kiểm tra nếu API trả về mã trạng thái không thành công
            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.error("Lỗi: Không thể thêm danh mục vào Moodle. Mã trạng thái: {}", response.getStatusCode());
                return "Lỗi: Không thể thêm danh mục vào Moodle.";
            }

            // Lấy ID từ phản hồi JSON
            int moodleId = extractMoodleId(response.getBody());
            if (moodleId == -1) {
                logger.error("Lỗi: Không thể lấy ID từ phản hồi Moodle.");
                return "Lỗi: Không thể thêm danh mục vào Moodle.";
            }

            // Chỉ thêm vào CSDL nếu thêm vào Moodle thành công
            addCategoryToDatabase(name, info, parentId, courseId, moodleId);
            return "Thêm danh mục thành công.";
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu xảy ra lỗi khi gửi yêu cầu API
            logger.error("Lỗi: Không thể thêm danh mục câu hỏi. Chi tiết: {}", e.getMessage());
            return "Lỗi: Không thể thêm danh mục câu hỏi. Vui lòng thử lại sau.";
        }
    }

    private int extractMoodleId(String jsonResponse) {
        try {
            JsonNode rootNode = new ObjectMapper().readTree(jsonResponse);

            if (!rootNode.has("category_id")) {
                logger.error("Phản hồi từ Moodle không có trường 'category_id'.");
                return -1;
            }

            return rootNode.path("category_id").asInt();
        } catch (JsonProcessingException e) {
            logger.error("Lỗi khi phân tích JSON: {}", e.getMessage());
            return -1;
        }
    }

    public String importCategoriesFromExcel(MultipartFile file, int courseId) {
        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            return "Lỗi: File không đúng định dạng .xlsx";
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int parentCategoryId = 0;

            for (int i = 3; i <= sheet.getLastRowNum(); i++) { // Bắt đầu từ hàng thứ 4
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Cột 1 - danh mục cha (parent = 0)
                Cell parentCell = row.getCell(0);
                if (parentCell != null && !parentCell.getStringCellValue().trim().isEmpty()) {
                    String parentCategoryName = parentCell.getStringCellValue().trim();
                    parentCategoryId = addCategory1(parentCategoryName, "", 0, courseId);
                    if (parentCategoryId == -1) {
                        return "Lỗi khi thêm danh mục cha vào Moodle.";
                    }
                }

                // Cột 2 - danh mục con của cột 1
                Cell childCell = row.getCell(1);
                int childCategoryId = parentCategoryId; // ID của danh mục cha cho các danh mục con tiếp theo
                if (childCell != null && !childCell.getStringCellValue().trim().isEmpty()) {
                    String childCategoryName = childCell.getStringCellValue().trim();
                    childCategoryId = addCategory1(childCategoryName, "", parentCategoryId, courseId);
                    if (childCategoryId == -1) {
                        return "Lỗi khi thêm danh mục con vào Moodle.";
                    }
                }

                // Cột 3 đến cột 6 - các danh mục con của cột 2
                for (int j = 2; j <= 5; j++) { // Tương ứng với cột 3 đến cột 6
                    Cell subChildCell = row.getCell(j);
                    if (subChildCell != null && !subChildCell.getStringCellValue().trim().isEmpty()) {
                        String subChildCategoryName = subChildCell.getStringCellValue().trim();
                        int subChildCategoryId = addCategory1(subChildCategoryName, "", childCategoryId, courseId);
                        if (subChildCategoryId == -1) {
                            return "Lỗi khi thêm danh mục phụ vào Moodle.";
                        }
                    }
                }
            }
            return "Import thành công.";
        } catch (IOException e) {
            return "Lỗi khi đọc file Excel: " + e.getMessage();
        }
    }

    public int addCategory1(String name, String info, int parentId, int courseId) {
        String apiMoodleFunc = "local_question_add_categories";
        String url = UriComponentsBuilder.fromHttpUrl(domainName + "/webservice/rest/server.php")
                .queryParam("wstoken", token)
                .queryParam("wsfunction", apiMoodleFunc)
                .queryParam("moodlewsrestformat", "json")
                .toUriString();

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("name", name);
        requestBody.add("info", info);
        requestBody.add("parent", String.valueOf(parentId));
        requestBody.add("courseid", String.valueOf(courseId));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.error("Lỗi: Không thể thêm danh mục vào Moodle. Mã trạng thái: {}", response.getStatusCode());
                return -1; // Trả về -1 nếu lỗi
            }

            int moodleId = extractMoodleId(response.getBody());
            if (moodleId == -1) {
                logger.error("Lỗi: Không thể lấy ID từ phản hồi Moodle.");
                return -1;
            }

            // Chỉ thêm vào CSDL nếu thêm vào Moodle thành công
            addCategoryToDatabase(name, info, parentId, courseId, moodleId);
            return moodleId; // Trả về ID của danh mục được thêm vào Moodle
        } catch (Exception e) {
            logger.error("Lỗi: Không thể thêm danh mục câu hỏi. Chi tiết: {}", e.getMessage());
            return -1;
        }
    }


    // Phương thức để thêm danh mục vào CSDL
    private void addCategoryToDatabase(String name, String info, int parent, int courseId, int moodleId) {
        // Thực hiện thêm danh mục vào CSDL
        QuestionCategoriesEntity category = new QuestionCategoriesEntity();
        category.setName(name);
        category.setInfo(info);
        category.setParent(parent);
        category.setCourseId(courseId);
        category.setMoodleId(moodleId); // Lưu ID của Moodle vào cột moodle_id
        // Ghi log thông tin trước khi lưu
        logger.info("Saving category: {} with moodle_id: {}", category.getName(), moodleId);
        // Lưu vào cơ sở dữ liệu
        QuestionCategoriesRepository.save(category);
    }
//    Xóa danh mục
    private boolean parseDeleteResponse(String responseBody) {
        try {
            // Giả sử Moodle trả về JSON với trường "status" hoặc "success"
            JSONObject jsonResponse = new JSONObject(responseBody);
            String status = jsonResponse.optString("status", "failure");
            return "success".equalsIgnoreCase(status);
        } catch (JSONException e) {
            logger.error("Lỗi khi phân tích phản hồi từ Moodle: {}", e.getMessage());
            return false;
        }
    }

    public String deleteCategory(int moodleId) {
        String apiMoodleFunc = "local_question_delete_category";
        String url = UriComponentsBuilder.fromHttpUrl(domainName + "/webservice/rest/server.php")
                .queryParam("wstoken", token)
                .queryParam("wsfunction", apiMoodleFunc)
                .queryParam("moodlewsrestformat", "json")
                .queryParam("category_id", moodleId)
                .toUriString();

        logger.info("Deleting category with ID {} from Moodle via URL: {}", moodleId, url);

        try {
            // Gửi yêu cầu xóa đến Moodle
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                boolean isDeleted = parseDeleteResponse(response.getBody());
                if (isDeleted) {
                    // Xóa danh mục trên Web sau khi xóa thành công trên Moodle
                    boolean isWebDeleted = deleteCategoryFromWeb(moodleId);
                    if (isWebDeleted) {
                        return "Xóa danh mục thành công trên Moodle và Web.";
                    } else {
                        return "Xóa danh mục trên Moodle thành công, nhưng không thể xóa trên Web.";
                    }
                } else {
                    logger.error("Moodle không xóa được danh mục với ID {}. Phản hồi: {}", moodleId, response.getBody());
                    return "Lỗi: Moodle không xóa được danh mục.";
                }
            } else {
                logger.error("Lỗi khi gọi API Moodle để xóa danh mục. Mã trạng thái: {}", response.getStatusCode());
                return "Lỗi: Không thể xóa danh mục câu hỏi trên Moodle. Mã trạng thái: " + response.getStatusCode();
            }
        } catch (Exception e) {
            logger.error("Lỗi khi gọi API Moodle để xóa danh mục: {}", e.getMessage());
            return "Lỗi: Không thể xóa danh mục câu hỏi. Chi tiết: " + e.getMessage();
        }
    }

    // Phương thức xóa danh mục trên Web (có thể là gọi API hoặc thao tác với cơ sở dữ liệu)
    private boolean deleteCategoryFromWeb(int moodleId) {
        try {
            String url = "http://your-web-api/delete-category?id=" + moodleId;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return true;
            } else {
                logger.error("Không thể xóa danh mục trên Web. Mã trạng thái: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            logger.error("Lỗi khi xóa danh mục trên Web: {}", e.getMessage());
            return false;
        }
    }
//    public QuestionCategoriesEntity getCategoryById(Integer categoryId) {
//        return QuestionCategoriesRepository.findById(categoryId)
//                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại với ID: " + categoryId));
//    }


    private static final String MOODLE_API_URL = "http://localhost/moodle/webservice/rest/server.php";

    // Phương thức để cập nhật danh mục câu hỏi trong Moodle


    // Lấy danh mục theo ID
//    public QuestionCategoriesEntity getCategoryById(Integer categoryId) {
//        return QuestionCategoriesRepository.findById(categoryId)
//                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại với ID: " + categoryId));
//    }

    // Cập nhật danh mục trong Moodle

//    public String updateCategory(int categoryId, String name, String info, Integer parent, String token) {
//        // Chỉnh sửa URL để đưa thông tin 'info' vào chuỗi
//        String url = String.format("%s?wstoken=%s&moodlewsrestformat=json&wsfunction=local_question_update_category&categoryid=%d&name=%s&info=%s&parent=%d",
//                MOODLE_API_URL, token, categoryId, name, info, parent);
//
//        // Gọi API Moodle và nhận phản hồi
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
//
//        return response.getBody();
//    }
//    private static final String MOODLE_API_URL = "http://localhost/moodle/webservice/rest/server.php";
//    private final RestTemplate restTemplate = new RestTemplate();



//    public QuestionCategoriesEntity getCategoryByMoodleId(int moodle_id) {
//        return QuestionCategoriesRepository.findByMoodle_id(moodle_id)
//                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại với Moodle ID: " + moodle_id));
//    }

//    public Optional<QuestionCategoriesEntity> getCategoryByMoodleId(int moodleId) {
//        return QuestionCategoriesRepository.findByMoodle_id(moodleId); // Gọi đúng phương thức
//    }
//    public String updateCategory(int moodle_id, String name, String info, Integer parent, String token) {
//        String url = String.format(
//                "%s?wstoken=%s&moodlewsrestformat=json&wsfunction=local_question_update_category&categoryid=%d&name=%s&info=%s&parent=%d",
//                MOODLE_API_URL, token, moodle_id, name, info, parent
//        );
//
//        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//        return response.getBody();

//    public boolean updateCategory(int categoryId, String name, int contextId, String info, int parent) {
//        // Tạo tham số cho API
//        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(MOODLE_API_URL)
//                .queryParam("wstoken", token)
//                .queryParam("moodlewsrestformat", "json")
//                .queryParam("wsfunction", "local_question_update_category")
//                .queryParam("categoryid", categoryId)
//                .queryParam("name", name)
//                .queryParam("contextid", contextId)
//                .queryParam("info", info)
//                .queryParam("parent", parent);
//
//        try {
//            // Gửi yêu cầu HTTP
//            ResponseEntity<String> response = restTemplate.getForEntity(builder.toUriString(), String.class);
//
//            // Kiểm tra kết quả từ API Moodle
//            return response.getStatusCode() == HttpStatus.OK;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }

    private static final String MOODLE_API_URLq = "http://localhost/moodle/webservice/rest/server.php?wstoken=54df098d9366c247f13f81e27f6dddb2&moodlewsrestformat=json&wsfunction=local_question_update_category";

    public String updateCategory(int moodleId, String name, int contextId, String info, int parent) {
        String apiUrl = MOODLE_API_URLq + "&categoryid=" + moodleId +
                "&name=" + name +
                "&contextid=" + contextId +
                "&info=" + info +
                "&parent=" + parent;

        try {
            // Gọi API Moodle để cập nhật danh mục
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, String.class);

            // Kiểm tra phản hồi từ API
            String responseBody = response.getBody();
//            String responseBody = response.getBody();
            System.out.println("Response from Moodle: " + responseBody);
            if (response.getStatusCode().is2xxSuccessful() && responseBody.contains("success")) {
                return "Cập nhật danh mục thành công!";
            } else {
                return "Cập nhật danh mục thất bại! Phản hồi: " + responseBody;
            }
        } catch (Exception e) {
            return "Lỗi khi gọi API: " + e.getMessage();
        }
    }

}