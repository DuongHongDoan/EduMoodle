package com.example.edumoodle.Controller.teacher;

import com.example.edumoodle.DTO.QuestionCategoriesDTO;
import com.example.edumoodle.Model.QuestionCategoriesEntity;
import com.example.edumoodle.Service.QuestionCategoriesService;
import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;


@Controller
public class QuestionCategoriesController {


    private final QuestionCategoriesService categoriesService;

    public QuestionCategoriesController(QuestionCategoriesService categoriesService) {
        this.categoriesService = categoriesService;
    }

    @GetMapping("/teacher/courses/view/list_question")
    public String listQuestionCategories(@RequestParam("courseId") int courseId, Model model) {
        List<QuestionCategoriesDTO> categories = categoriesService.getQuestionCategories(courseId);
        model.addAttribute("questionCategories", categories);
        model.addAttribute("courseId", courseId);
        System.out.println("Thông báo thành công: " + model.getAttribute("response"));
        System.out.println("Thông báo lỗi: " + model.getAttribute("error"));
        return "teacher/list_question"; // Trả về template mới
    }
//@GetMapping("/teacher/courses/view/list_question")
//public String listQuestionCategories(@RequestParam("courseId") int courseId,
//                                     @RequestParam(required = false) Integer page,
//                                     Model model) {
//    if (page == null) {
//        page = 0; // Set default page
//    }
//
//    Page<QuestionCategoriesEntity> pageResult = categoriesService.getPaginatedCategories(courseId, page);
//    model.addAttribute("questionCategories", pageResult.getContent());
//    model.addAttribute("currentPage", page);
//    model.addAttribute("totalPages", pageResult.getTotalPages());
//    return "teacher/list_question";
//}

    //    thêm danh mục câu hỏi
    @GetMapping("/teacher/courses/view/Add_QuestionCategories")
    public String showAddCategoryForm(@RequestParam("courseId") int courseId, Model model) {
        List<QuestionCategoriesDTO> parentCategories = categoriesService.getQuestionCategories(courseId);
        model.addAttribute("parentCategories", parentCategories);
        model.addAttribute("courseId", courseId);
        return "teacher/Add_QuestionCategories";
    }

    @PostMapping("/teacher/courses/view/Add_QuestionCategories")
    public String addCategory(
            @RequestParam("name") String name,
            @RequestParam(value = "info", required = false, defaultValue = "") String info,
            @RequestParam("parent") int parent,
            @RequestParam("courseid") int courseId,
            RedirectAttributes redirectAttributes) {

        // Gọi service để thêm danh mục
        String response = categoriesService.addCategory(name, info, parent, courseId);

        // Kiểm tra nếu thêm không thành công (giả sử chuỗi "Error" xuất hiện trong phản hồi lỗi)
        if (response.contains("Error")) {
            redirectAttributes.addFlashAttribute("error", response);
            System.out.println("Added response: " + redirectAttributes.getFlashAttributes().get("error"));

            return "redirect:/teacher/courses/view/Add_QuestionCategories?courseId=" + courseId;
        }
        // Nếu thêm thành công, gửi thông báo thành công
        redirectAttributes.addFlashAttribute("response", "Danh mục đã được thêm thành công!");
        System.out.println("Added response: " + redirectAttributes.getFlashAttributes().get("response"));

        return "redirect:/teacher/courses/view/list_question?courseId=" + courseId;
    }
//    import file Excel
@PostMapping("/uploadExcel")
public String uploadExcel(@RequestParam("file") MultipartFile file,
                          @RequestParam("courseId") int courseId,
                          @RequestParam("parentCategoryForColumn2") int parentCategoryForColumn2) {
    String result = categoriesService.importCategoriesFromExcel(file, courseId, parentCategoryForColumn2);
    return "redirect:/teacher/courses/view/list_question?courseId=" + courseId;// Hiển thị thông báo kết quả import
}

//    Xóa danh mục
    @PostMapping("/teacher/courses/view/Delete_QuestionCategory/{moodle_id}")

    public String deleteCategory(@PathVariable Integer moodle_id,
                                 @RequestParam("courseId") int courseId,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Xử lý xóa danh mục
            categoriesService.deleteCategory(moodle_id);
            // Thêm thông báo thành công vào RedirectAttributes
            redirectAttributes.addFlashAttribute("response", "Xóa danh mục thành công!");
        } catch (RuntimeException e) {
            // Thêm thông báo lỗi vào RedirectAttributes
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        // Redirect lại trang danh sách với courseId
        return "redirect:/teacher/courses/view/list_question?courseId=" + courseId;
    }

    //cập nhật thông tin danh mục
    @GetMapping("/teacher/courses/view/update/{moodleId}")
//    public String editCategory(@PathVariable("moodleId") int moodleId,
//                               @RequestParam("courseId") int courseId,
//                               Model model) {
//        // Tạo URL cho API lấy thông tin danh mục
//        String url = "http://localhost/moodle/webservice/rest/server.php";
//        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
//                .queryParam("wstoken", "54df098d9366c247f13f81e27f6dddb2")
//                .queryParam("moodlewsrestformat", "json")
//                .queryParam("wsfunction", "local_question_get_categories")
//                .queryParam("courseId", courseId);
//
//        // Tạo RestTemplate và gọi API
//        RestTemplate restTemplate = new RestTemplate();
//        Map<String, Object> response = restTemplate.getForObject(builder.toUriString(), Map.class);
//
//        // Kiểm tra và lấy danh mục cần thiết từ response
//        if (response != null && response.containsKey("categories")) {
//            List<Map<String, Object>> categories = (List<Map<String, Object>>) response.get("categories");
//
//            // Lấy danh mục tương ứng với `moodleId`
//            for (Map<String, Object> category : categories) {
//                if ((int) category.get("id") == moodleId) {
//                    model.addAttribute("name", category.get("name"));
//                    model.addAttribute("info", category.get("info"));
//                    model.addAttribute("contextId", category.get("contextid"));
//                    model.addAttribute("parent", category.get("parent"));
//                    break;
//                }
//            }
//        }
//
//        // Truyền `moodleId` và `courseId` vào model để sử dụng trong form
//        model.addAttribute("moodleId", moodleId);
//        model.addAttribute("courseId", courseId);
//
//        return "teacher/Edit_QuestionCategory";  // Trang chỉnh sửa danh mục câu hỏi
//    }
//    @GetMapping("/teacher/courses/view/update/{moodleId}")
    public String editCategory(@PathVariable("moodleId") int moodleId,
                               @RequestParam("courseId") int courseId,
                               Model model) {
        // Tạo URL cho API lấy thông tin danh mục
        String url = "http://localhost/moodle/webservice/rest/server.php";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("wstoken", "54df098d9366c247f13f81e27f6dddb2")
                .queryParam("moodlewsrestformat", "json")
                .queryParam("wsfunction", "local_question_get_categories")
                .queryParam("courseId", courseId);

        // Tạo RestTemplate và gọi API
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(builder.toUriString(), Map.class);

        // Kiểm tra và lấy danh mục cần thiết từ response
        List<Map<String, Object>> categories = new ArrayList<>();
        if (response != null && response.containsKey("categories")) {
            categories = (List<Map<String, Object>>) response.get("categories");

            // Lấy danh mục tương ứng với `moodleId`
            for (Map<String, Object> category : categories) {
                if ((int) category.get("id") == moodleId) {
                    model.addAttribute("name", category.get("name"));
                    model.addAttribute("info", category.get("info"));
                    model.addAttribute("contextId", category.get("contextid"));
                    model.addAttribute("parent", category.get("parent"));
                    break;
                }
            }
        }

        // Truyền danh sách các danh mục vào model để hiển thị tên danh mục cha
        model.addAttribute("categories", categories);

        // Truyền `moodleId` và `courseId` vào model để sử dụng trong form
        model.addAttribute("moodleId", moodleId);
        model.addAttribute("courseId", courseId);

        return "teacher/Edit_QuestionCategory";  // Trang chỉnh sửa danh mục câu hỏi
    }



    // This is the correct POST mapping
    @PostMapping("/teacher/courses/view/update/{moodleId}")
    public String updateCategory(@PathVariable("moodleId") int moodleId,
                                 @RequestParam("courseId") int courseId,
                                 @RequestParam("name") String name,
                                 @RequestParam("contextId") int contextId,
                                 @RequestParam("info") String info,
                                 @RequestParam("parent") int parent,
                                 RedirectAttributes redirectAttributes) {

        // Gọi service để cập nhật danh mục câu hỏi
        System.out.println("Moodle ID: " + moodleId);
        System.out.println("Name: " + name);
        System.out.println("Context ID: " + contextId);
        System.out.println("Info: " + info);
        System.out.println("Parent ID: " + parent);
        String message = categoriesService.updateCategory(moodleId, name, contextId, info, parent);

        // Thêm thông báo cho RedirectAttributes
        if (message.contains("thành công")) {
            redirectAttributes.addFlashAttribute("response","Chỉnh sửa danh mục thành công");
        } else {
            redirectAttributes.addFlashAttribute("error", message);
        }

        // Quay lại trang danh sách câu hỏi
        return "redirect:/teacher/courses/view/list_question?courseId=" + courseId;
    }
@GetMapping("/api/questions/export")
public ResponseEntity<byte[]> exportQuestionCategoriesToExcel1(@RequestParam("courseId") int courseId) throws IOException {
    List<QuestionCategoriesDTO> categories = categoriesService.getQuestionCategories(courseId);

    // Khởi tạo Map để nhóm danh mục theo parent ID
    Map<Integer, List<QuestionCategoriesDTO>> categoryMap = new HashMap<>();

    // Nhóm danh mục theo parent
    for (QuestionCategoriesDTO category : categories) {
        Integer parentId = category.getParent();
        categoryMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(category);
    }

    // Tạo workbook
    Workbook workbook = new XSSFWorkbook();

    // Xuất dữ liệu ra Excel theo cấu trúc cây
    int sheetIdx = 0;
    for (Map.Entry<Integer, List<QuestionCategoriesDTO>> entry : categoryMap.entrySet()) {
        Integer parentId = entry.getKey();
        List<QuestionCategoriesDTO> children = entry.getValue();

        // Tạo một sheet mới cho mỗi nhóm danh mục con
        Sheet sheet = workbook.createSheet("Danh mục cha " + parentId);

        // Tạo header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Danh mục cha");
        headerRow.createCell(1).setCellValue("Danh mục con");
        headerRow.createCell(2).setCellValue("Danh mục con của con");
        headerRow.createCell(3).setCellValue("Danh mục con của con của con");

        // Ghi các danh mục vào sheet
        int rowIdx = 1;
        for (QuestionCategoriesDTO parent : children) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(parent.getName()); // Tên danh mục cha

            // Ghi các danh mục con vào các cột tiếp theo
            int columnIdx = 1;
            if (parent.getChildren() != null) {
                for (QuestionCategoriesDTO child : parent.getChildren()) {
                    row.createCell(columnIdx++).setCellValue(child.getName());

                    // Ghi danh mục con của con (nếu có)
                    if (child.getChildren() != null) {
                        for (QuestionCategoriesDTO grandchild : child.getChildren()) {
                            row.createCell(columnIdx++).setCellValue(grandchild.getName());
                        }
                    }
                }
            }
        }

        sheetIdx++;
    }

    // Xuất file Excel ra byte array
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    workbook.write(out);
    workbook.close();

    // Thiết lập header và trả về file Excel
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=question_categories.xlsx");

    return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(out.toByteArray());
}

@GetMapping("/exportQuestionCategoriesToExcel")
public ResponseEntity<InputStreamResource> exportQuestionCategoriesToExcel(@RequestParam("courseId") int courseId) throws IOException {
    // Lấy tất cả danh mục và cập nhật số lượng câu hỏi
    List<QuestionCategoriesDTO> categories = categoriesService.getAllQuestionCategories(courseId);

    if (categories == null || categories.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // Cập nhật số lượng câu hỏi cho từng danh mục và các danh mục con
    updateQuestionCountForCategories(categories);

    // Tạo một workbook mới
    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("Question Categories");

    // Cài đặt các tiêu đề cho các cột
    XSSFRow headerRow = sheet.createRow(0);
    headerRow.createCell(0).setCellValue(" ");
    headerRow.createCell(1).setCellValue("Tên ngân hàng");
    headerRow.createCell(2).setCellValue("Tên chủ đề");
    headerRow.createCell(3).setCellValue("Tên chủ điểm");
    headerRow.createCell(4).setCellValue(" ");
    headerRow.createCell(5).setCellValue("Nhận biết (1)");
    headerRow.createCell(6).setCellValue("Thông hiểu(2)" );
    headerRow.createCell(7).setCellValue("Vận dụng (3)");
    headerRow.createCell(8).setCellValue("Vận dụng ở mức cao (4)");


    // Xuất danh mục vào Excel
    int rowNum = 1;
    exportCategoriesToExcel(categories, sheet, rowNum, 0);

    // Lưu workbook vào ByteArrayOutputStream
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    workbook.write(outputStream);
    workbook.close();

    // Tạo header cho response để tải file
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=question_categories.xlsx");

    return ResponseEntity.ok()
            .headers(headers)
            .body(new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray())));
}

    // Hàm đệ quy cập nhật số lượng câu hỏi cho danh mục và các danh mục con
    private void updateQuestionCountForCategories(List<QuestionCategoriesDTO> categories) {
        for (QuestionCategoriesDTO category : categories) {
            int questionCount = categoriesService.getQuestionCountForCategory(category.getId());
            category.setQuestionCount(questionCount); // Cập nhật số lượng câu hỏi cho danh mục

            // Cập nhật cho các danh mục con
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                updateQuestionCountForCategories(category.getChildren());
            }
        }
    }

    // Hàm để tính tổng số câu hỏi của danh mục và tất cả các danh mục con của nó
    private int getTotalQuestionCount(QuestionCategoriesDTO category) {
        int total = category.getQuestionCount();
        if (category.getChildren() != null) {
            for (QuestionCategoriesDTO child : category.getChildren()) {
                total += getTotalQuestionCount(child);
            }
        }
        return total;
    }

    // Hàm này sẽ xuất các danh mục câu hỏi vào file Excel với các danh mục con nhỏ nhất nằm trên cùng một hàng ngang và có tên cố định
    // Hàm để xuất tiêu đề cho các cột mức độ
    private void setColumnHeaders(XSSFSheet sheet, int columnIndex) {
        XSSFRow headerRow = sheet.createRow(0);  // Đặt hàng tiêu đề ở dòng đầu tiên
        headerRow.createCell(columnIndex).setCellValue("Tên danh mục");
        headerRow.createCell(columnIndex + 1).setCellValue("Tổng số câu hỏi");
        headerRow.createCell(columnIndex + 2).setCellValue("Nhận biết (1)");
        headerRow.createCell(columnIndex + 3).setCellValue("Thông hiểu (2)");
        headerRow.createCell(columnIndex + 4).setCellValue("Vận dụng (3)");
        headerRow.createCell(columnIndex + 5).setCellValue("Vận dụng cao (4)");
    }

    // Hàm để xuất các danh mục câu hỏi vào file Excel
//    private int exportCategoriesToExcel(List<QuestionCategoriesDTO> categories, XSSFSheet sheet, int rowNum, int columnIndex) {
//        if (rowNum == 0) {
//            // Đặt tên tiêu đề cho các cột trước khi xuất dữ liệu
//            setColumnHeaders(sheet, columnIndex);
//            rowNum++;  // Tăng rowNum để bắt đầu xuất dữ liệu từ dòng thứ 2
//        }
//
//        for (QuestionCategoriesDTO category : categories) {
//            XSSFRow row = sheet.createRow(rowNum);
//
//            // Điền tên danh mục cha
//            String nameWithCount = category.getName() + " (" + category.getQuestionCount() + ")";
//            row.createCell(columnIndex).setCellValue(nameWithCount);
//
//            // Tính tổng số câu hỏi của danh mục cha và tất cả danh mục con, rồi hiển thị bên cạnh ô tên danh mục
//            int totalQuestionCount = getTotalQuestionCount(category);
//            row.createCell(columnIndex + 1).setCellValue("Tổng số câu hỏi: " + totalQuestionCount);
//
//            rowNum++; // Tăng dòng để ghi các danh mục con nằm dưới danh mục cha
//
//            // Nếu danh mục có danh mục con
//            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
//                // Kiểm tra nếu tất cả các danh mục con đều là mức nhỏ nhất (không có danh mục con của chúng)
//                boolean allChildrenAreLeaf = category.getChildren().stream().allMatch(child -> child.getChildren() == null || child.getChildren().isEmpty());
//
//                if (allChildrenAreLeaf) {
//                    // Nếu tất cả danh mục con là mức nhỏ nhất, ghi chúng trên cùng một hàng ngang dưới danh mục cha
//                    XSSFRow childRow = sheet.createRow(rowNum);
//                    int childColumn = columnIndex + 2;  // Bắt đầu từ cột "Nhận biết (1)"
//                    for (QuestionCategoriesDTO child : category.getChildren()) {
//                        String childNameWithCount = child.getName() + " (" + child.getQuestionCount() + ")";
//                        childRow.createCell(childColumn).setCellValue(childNameWithCount);
//                        childColumn++;
//                    }
//                    rowNum++; // Tăng dòng sau khi ghi tất cả danh mục con
//                } else {
//                    // Nếu có danh mục con lồng thêm, tiếp tục đệ quy gọi hàm cho từng danh mục con
//                    for (QuestionCategoriesDTO child : category.getChildren()) {
//                        rowNum = exportCategoriesToExcel(List.of(child), sheet, rowNum, columnIndex + 1);
//                    }
//                }
//            }
//        }
//        return rowNum;
//    }
    private int exportCategoriesToExcel(List<QuestionCategoriesDTO> categories, XSSFSheet sheet, int rowNum, int columnIndex) {
        if (rowNum == 0) {
            // Đặt tên tiêu đề cho các cột trước khi xuất dữ liệu
            setColumnHeaders(sheet, columnIndex);
            rowNum++;  // Tăng rowNum để bắt đầu xuất dữ liệu từ dòng thứ 2
        }

        for (QuestionCategoriesDTO category : categories) {
            XSSFRow row = sheet.createRow(rowNum);

            // Điền tên danh mục cha
            String nameWithCount = category.getName() + " (" + category.getQuestionCount() + ")";
            row.createCell(columnIndex).setCellValue(nameWithCount);

            // Tính tổng số câu hỏi của danh mục cha và tất cả danh mục con, rồi hiển thị bên cạnh ô tên danh mục
            int totalQuestionCount = getTotalQuestionCount(category);
            row.createCell(columnIndex + 1).setCellValue( totalQuestionCount);

            rowNum++; // Tăng dòng để ghi các danh mục con nằm dưới danh mục cha

            // Nếu danh mục có danh mục con
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                // Kiểm tra nếu tất cả các danh mục con đều là mức nhỏ nhất (không có danh mục con của chúng)
                boolean allChildrenAreLeaf = category.getChildren().stream().allMatch(child -> child.getChildren() == null || child.getChildren().isEmpty());

                if (allChildrenAreLeaf) {
                    // Nếu tất cả danh mục con là mức nhỏ nhất, ghi chúng trên cùng một hàng ngang dưới danh mục cha
                    XSSFRow childRow = sheet.createRow(rowNum);
                    int childColumn = columnIndex + 2;  // Bắt đầu từ cột "Nhận biết (1)"
                    for (QuestionCategoriesDTO child : category.getChildren()) {
                        int childNameWithCount = child.getQuestionCount() ;
                        childRow.createCell(childColumn).setCellValue(childNameWithCount);
                        childColumn++;
                    }
                    rowNum++; // Tăng dòng sau khi ghi tất cả danh mục con
                } else {
                    // Nếu có danh mục con lồng thêm, tiếp tục đệ quy gọi hàm cho từng danh mục con
                    for (QuestionCategoriesDTO child : category.getChildren()) {
                        rowNum = exportCategoriesToExcel(List.of(child), sheet, rowNum, columnIndex + 1);
                    }
                }
            }
        }
        return rowNum;
    }



}