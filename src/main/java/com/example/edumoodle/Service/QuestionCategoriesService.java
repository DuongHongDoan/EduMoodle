package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.*;
import com.example.edumoodle.Mapper.*;
import com.example.edumoodle.DTO.QuestionCategoriesResponseDTO;
import com.example.edumoodle.Model.QuestionCategoriesEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import com.example.edumoodle.Repository.QuestionCategoriesRepository; // Thay thế với đường dẫn đúng
import java.io.IOException;
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
        @Autowired
        private QuestionCategoriesRepository questionCategoriesRepository;
//Phân trang
    public Page<QuestionCategoriesEntity> getPaginatedCategories(int courseId, int page) {
    // Giới hạn mỗi trang 5 danh mục
        PageRequest pageRequest = PageRequest.of(page, 5);
        return questionCategoriesRepository.findByCourseId(courseId, pageRequest);
    }

// Đếm số lượng câu hỏi
    public int getQuestionCountForCategory(int questionCategoryId) {
        String apiMoodleFunc = "local_question_get_question_by_category";
        String url = String.format("%s/webservice/rest/server.php?wstoken=%s&wsfunction=%s&moodlewsrestformat=json&questioncategoryid=%d",
                domainName, token, apiMoodleFunc, questionCategoryId);

        try {
            // Gửi yêu cầu đến API và nhận danh sách câu hỏi trong danh mục
            ResponseEntity<QuestionsResponseDTO> response = restTemplate.getForEntity(url, QuestionsResponseDTO.class);
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                logger.error("Không có câu hỏi trong danh mục hoặc yêu cầu thất bại.");
                return 0;
            }

            // Trả về số lượng câu hỏi trong danh mục
            List<QuestionsDTO> questions = response.getBody().getQuestions();
            logger.info("Số lượng câu hỏi trong danh mục {}: {}", questionCategoryId, (questions != null) ? questions.size() : 0);
            logger.info("URL yêu cầu đến API lấy câu hỏi: {}", url);
            logger.info("Phản hồi từ API: {}", response.getBody());
            return (questions != null) ? questions.size() : 0;
        } catch (Exception e) {
            logger.error("Lỗi khi gọi API Moodle: {}", e.getMessage());
            return 0;
        }
    }

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
            // Cập nhật số lượng câu hỏi trong mỗi danh mục
            for (QuestionCategoriesDTO category : categories) {
                category.setQuestionCount(getQuestionCountForCategory(category.getId())); // Chỉ truyền questionCategoryId
            }
            return buildCategoryTree(categories);
        }
        catch (Exception e) {
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
                // Thêm danh mục cha vào danh sách gốc (root)
                rootCategories.add(category);
            } else {
                // Nếu có parent, thêm vào danh sách children của parent
                QuestionCategoriesDTO parent = categoryMap.get(category.getParent());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(category);
                } else {
                    // Nếu không tìm thấy parent, vẫn thêm category vào danh sách root
                    // Điều này đảm bảo rằng danh mục con không có cha vẫn hiển thị
                    rootCategories.add(category);
                }
            }
        }

        return rootCategories;
    }

    public List<QuestionCategoriesDTO> getAllQuestionCategories(int courseId) {
        String apiMoodleFunc = "local_question_get_categories";
        String url = String.format("%s/webservice/rest/server.php?wstoken=%s&wsfunction=%s&moodlewsrestformat=json&courseId=%d",
                domainName, token, apiMoodleFunc, courseId);

        try {
            // Gửi yêu cầu đến API và nhận kết quả
            ResponseEntity<QuestionCategoriesResponseDTO> response = restTemplate.getForEntity(url, QuestionCategoriesResponseDTO.class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                return Collections.emptyList();
            }

            // Kiểm tra trạng thái phản hồi
            if (!"success".equalsIgnoreCase(response.getBody().getStatus())) {
                return Collections.emptyList();
            }

            // Trả về danh sách các danh mục câu hỏi
            List<QuestionCategoriesDTO> categories = response.getBody().getCategories();
            if (categories == null) {
                return Collections.emptyList();
            }

            // Tạo một Map để nhóm các danh mục theo parentId
            Map<Integer, List<QuestionCategoriesDTO>> categoryMap = new HashMap<>();

            // Nhóm danh mục theo parentId
            for (QuestionCategoriesDTO category : categories) {
                categoryMap.computeIfAbsent(category.getParent(), k -> new ArrayList<>()).add(category);
            }

            // Xây dựng cấu trúc cây cha-con
            List<QuestionCategoriesDTO> rootCategories = new ArrayList<>();
            for (QuestionCategoriesDTO category : categories) {
                if (category.getParent() == 0) { // Danh mục gốc
                    buildCategoryTree(category, categoryMap);
                    rootCategories.add(category);
                }
            }

            return rootCategories;

        } catch (Exception e) {
            // Xử lý lỗi ngoại lệ nếu xảy ra
            return Collections.emptyList();
        }
    }

    // Xây dựng cấu trúc cây cha-con
    private void buildCategoryTree(QuestionCategoriesDTO category, Map<Integer, List<QuestionCategoriesDTO>> categoryMap) {
        List<QuestionCategoriesDTO> children = categoryMap.get(category.getId());
        if (children != null) {
            category.setChildren(children);
            for (QuestionCategoriesDTO child : children) {
                buildCategoryTree(child, categoryMap); // Đệ quy cho các danh mục con
            }
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

    public String importCategoriesFromExcel(MultipartFile file, int courseId, int selectedParentCategoryId) {
        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            return "Lỗi: File không đúng định dạng .xlsx";
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Đọc ô B2 và B3 để quyết định tên danh mục
            Row rowB2 = sheet.getRow(1); // Hàng thứ 2 (B2)
            Row rowB3 = sheet.getRow(2); // Hàng thứ 3 (B3)
            String parentCategoryName = null;
            int parentCategoryIdFromB2 = selectedParentCategoryId; // Sử dụng `selectedParentCategoryId` làm cha của B2

            if (rowB2 != null) {
                Cell parentCellB2 = rowB2.getCell(1); // Cột B, tức là cột 2
                String parentCategoryNameFromB2 = (parentCellB2 != null) ? parentCellB2.getStringCellValue().trim() : null;

                Cell parentCellB3 = rowB3 != null ? rowB3.getCell(1) : null; // Cột B của B3
                String parentCategoryNameFromB3 = (parentCellB3 != null) ? parentCellB3.getStringCellValue().trim() : null;

                // Quyết định tên danh mục dựa trên các điều kiện
                if (parentCategoryNameFromB2 != null && parentCategoryNameFromB3 == null) {
                    parentCategoryName = parentCategoryNameFromB2; // Sử dụng B2 nếu B3 trống
                } else if (parentCategoryNameFromB3 != null && parentCategoryNameFromB2 == null) {
                    parentCategoryName = parentCategoryNameFromB3; // Sử dụng B3 nếu B2 trống
                } else if (parentCategoryNameFromB2 != null && parentCategoryNameFromB3 != null) {
                    parentCategoryName = parentCategoryNameFromB2 + " - " + parentCategoryNameFromB3; // Nối B2 và B3 bằng dấu gạch nối
                }
            }
            // Nếu có tên danh mục, tạo danh mục mới với parent là `selectedParentCategoryId`
            if (parentCategoryName != null) {
                parentCategoryIdFromB2 = addCategory1(parentCategoryName, "", selectedParentCategoryId, courseId);
                if (parentCategoryIdFromB2 == -1) {
                    return "Lỗi khi thêm danh mục cha từ ô B2 vào Moodle.";
                }
            }
            int currentParentIdFromColumnA = parentCategoryIdFromB2; // Giữ ID danh mục cha gần nhất từ cột A
            int currentParentIdFromColumnB = -1; // Giữ ID danh mục cha gần nhất từ cột B

            // Bắt đầu từ hàng thứ 7 để xử lý các danh mục
            for (int i = 4; i <= sheet.getLastRowNum(); i++) { // Bắt đầu từ hàng thứ 7
                Row row = sheet.getRow(i);
                if (row == null) continue;
                // Cột 1 (A) - danh mục cha gần nhất
                Cell parentCell = row.getCell(0);
                if (parentCell != null && !parentCell.getStringCellValue().trim().isEmpty()) {
                    String parentCategoryNameFromColumnA = parentCell.getStringCellValue().trim();
                    currentParentIdFromColumnA = addCategory1(parentCategoryNameFromColumnA, "", parentCategoryIdFromB2, courseId);
                    if (currentParentIdFromColumnA == -1) {
                        return "Lỗi khi thêm danh mục cha vào Moodle.";
                    }
                    currentParentIdFromColumnB = -1; // Reset danh mục cha gần nhất của cột B
                }
                // Cột 2 (B) - danh mục con của cột 1 gần nhất
                Cell childCell = row.getCell(1);
                if (childCell != null && !childCell.getStringCellValue().trim().isEmpty()) {
                    String childCategoryName = childCell.getStringCellValue().trim();
                    currentParentIdFromColumnB = addCategory1(childCategoryName, "", currentParentIdFromColumnA, courseId);
                    if (currentParentIdFromColumnB == -1) {
                        return "Lỗi khi thêm danh mục con vào Moodle.";
                    }
                }
                // Cột 3 đến cột 6 (C-F) - các danh mục con của cột 2 gần nhất
                for (int j = 2; j <= 5; j++) { // Tương ứng với cột 3 đến cột 6
                    Cell subChildCell = row.getCell(j);
                    if (subChildCell != null && !subChildCell.getStringCellValue().trim().isEmpty()) {
                        String subChildCategoryName = "Mức " + (j - 1) + ": " + subChildCell.getStringCellValue().trim();
                        int subChildCategoryId = addCategory1(subChildCategoryName, "", currentParentIdFromColumnB, courseId);
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
                    // Xóa danh mục trực tiếp trong cơ sở dữ liệu Web
                    questionCategoriesRepository.deleteByMoodleId(moodleId);
                    return "Xóa danh mục thành công trên Moodle và Web.";
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


    public String updateCategory(int moodleId, String name, int contextId, String info, int parent) {
        String apiMoodleFunc = "local_question_update_category";
        String apiUrl = String.format("%s/webservice/rest/server.php?wstoken=%s&wsfunction=%s&moodlewsrestformat=json&categoryid=%d&name=%s&contextid=%d&info=%s&parent=%d",
                domainName, token, apiMoodleFunc, moodleId, name, contextId, info, parent);

        try {
            // Gọi API Moodle để cập nhật danh mục
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, String.class);

            // Kiểm tra phản hồi từ API
            String responseBody = response.getBody();
            System.out.println("Response from Moodle: " + responseBody);

            if (response.getStatusCode().is2xxSuccessful() && responseBody.contains("success")) {
                // Cập nhật danh mục trực tiếp trong cơ sở dữ liệu Web
                int updatedRows = questionCategoriesRepository.updateCategoryByMoodleId(name, contextId, info, parent, moodleId);

                if (updatedRows > 0) {
                    return "Cập nhật danh mục thành công trên Moodle và Web!";
                } else {
                    return "Cập nhật trên Moodle thành công, nhưng không thể cập nhật trên Web.";
                }
            } else {
                return "Cập nhật danh mục thất bại! Phản hồi từ Moodle: " + responseBody;
            }
        } catch (Exception e) {
            return "Lỗi khi gọi API: " + e.getMessage();
        }
    }

}