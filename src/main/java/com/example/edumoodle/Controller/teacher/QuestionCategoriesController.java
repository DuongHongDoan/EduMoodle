package com.example.edumoodle.Controller.teacher;

import com.example.edumoodle.DTO.QuestionCategoriesDTO;
import com.example.edumoodle.Service.QuestionCategoriesService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class QuestionCategoriesController {
    @Value("${moodle.token}")
    private String token;

    @Value("${moodle.domainName}")
    private String domainName;

    @Autowired
    private QuestionCategoriesService categoriesService;

    private final RestTemplate restTemplate;
    public QuestionCategoriesController() {
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/manage/quiz/view")
    public String getQuizView(@RequestParam Integer courseId, @RequestParam Integer quizId, Model model) {
        return "common/QuizDetail";
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



    @PostMapping("/uploadExcel")
    public String uploadExcel(@RequestParam("file") MultipartFile file,
                              @RequestParam("courseId") int courseId,
                              @RequestParam("parentCategoryForColumn2") int parentCategoryForColumn2,
                              RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn file Excel!");
            return "redirect:/teacher/courses/view/list_question?courseId=" + courseId;
        }

        String result = categoriesService.importCategoriesFromExcel(file, courseId, parentCategoryForColumn2);
        redirectAttributes.addFlashAttribute("response", result);
        return "redirect:/teacher/courses/view/list_question?courseId=" + courseId;
    }

    @GetMapping("/teacher/courses/view/update/{moodleId}")
    public String editCategory(@PathVariable("moodleId") int moodleId,
                               @RequestParam("courseId") int courseId,
                               Model model) {
        List<Map<String, Object>> categories = categoriesService.getCategories(courseId);

        // Tìm danh mục cần chỉnh sửa
        for (Map<String, Object> category : categories) {
            if ((int) category.get("id") == moodleId) {
                model.addAttribute("name", category.get("name"));
                model.addAttribute("info", category.get("info"));
                model.addAttribute("contextId", category.get("contextid"));
                model.addAttribute("parent", category.get("parent"));
                break;
            }
        }

        // Truyền danh sách các danh mục vào model
        model.addAttribute("categories", categories);
        model.addAttribute("moodleId", moodleId);
        model.addAttribute("courseId", courseId);

        return "teacher/Edit_QuestionCategory";  // Trang chỉnh sửa danh mục câu hỏi
    }

    @PostMapping("/teacher/courses/view/update/{moodleId}")
    public String updateCategory(@PathVariable("moodleId") int moodleId,
                                 @RequestParam("courseId") int courseId,
                                 @RequestParam("name") String name,
                                 @RequestParam("contextId") int contextId,
                                 @RequestParam("info") String info,
                                 @RequestParam("parent") int parent,
                                 RedirectAttributes redirectAttributes) {

        String message = categoriesService.updateCategory(moodleId, name, contextId, info, parent);

        // Thêm thông báo cho RedirectAttributes
        if (message.contains("thành công")) {
            redirectAttributes.addFlashAttribute("response", "Chỉnh sửa danh mục thành công");
        } else {
            redirectAttributes.addFlashAttribute("error", message);
        }

        return "redirect:/teacher/courses/view/list_question?courseId=" + courseId;
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
        headerRow.createCell(0).setCellValue("THỐNG KÊ CÂU HỎI NGÂN HÀNG ĐỀ THI");
//        headerRow.createCell(1).setCellValue("Tên ngân hàng");
//        headerRow.createCell(2).setCellValue("Tên chủ đề");
//        headerRow.createCell(3).setCellValue("Tên chủ điểm");
//        headerRow.createCell(4).setCellValue("Nhận biết (1)");
//        headerRow.createCell(5).setCellValue("Thông hiểu(2)" );
//        headerRow.createCell(6).setCellValue("Vận dụng (3)");
//        headerRow.createCell(7).setCellValue("Vận dụng ở mức cao (4)");


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

    private int exportCategoriesToExcel(List<QuestionCategoriesDTO> categories, XSSFSheet sheet, int rowNum, int columnIndex) {
        for (QuestionCategoriesDTO category : categories) {
            // Bỏ qua danh mục gốc, chỉ xử lý danh mục con của danh mục gốc.
            if (category.getParent() == 0) {
                // Tiếp tục xử lý các danh mục con.
                if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                    for (QuestionCategoriesDTO child : category.getChildren()) {
                        rowNum = exportCategoriesToExcel(List.of(child), sheet, rowNum, columnIndex);
                    }
                }
                continue; // Không xuất danh mục gốc ra file Excel.
            }

            // Kiểm tra danh mục có phải là con của danh mục gốc và có chứa " - ".
            if (category.getParent() != 0 && category.getName().contains(" - ")) {
                String[] parts = category.getName().split(" - ", 2);
                String name = parts[0].trim(); // Tên danh mục.
                String code = parts[1].trim(); // Mã danh mục.

                // Ghi thông tin "Tên" vào dòng đầu tiên.
                XSSFRow nameRow = sheet.createRow(rowNum);
                nameRow.createCell(columnIndex).setCellValue("Tên:");
                nameRow.createCell(columnIndex + 1).setCellValue(name);

                rowNum++; // Xuống dòng.

                // Ghi thông tin "Mã" vào dòng tiếp theo.
                XSSFRow codeRow = sheet.createRow(rowNum);
                codeRow.createCell(columnIndex).setCellValue("Mã:");
                codeRow.createCell(columnIndex + 1).setCellValue(code);

                rowNum++;// Xuống dòng tiếp theo cho danh mục tiếp theo.
                // Thêm hàng tiêu đề cho các cột "Chủ đề", "Chủ điểm", và các cột "Mức độ"
                XSSFRow headerRow = sheet.createRow(rowNum);
                headerRow.createCell(0).setCellValue(" ");
                headerRow.createCell(1).setCellValue("Chủ đề");
                headerRow.createCell(2).setCellValue("Chủ điểm");
                headerRow.createCell(3).setCellValue("Nhận biết (1)");
                headerRow.createCell(4).setCellValue("Thông hiểu (2)");
                headerRow.createCell(5).setCellValue("Vận dụng (3)");
                headerRow.createCell(6).setCellValue("Vận dụng ở mức cao (4)");

                rowNum++; // Xuống dòng tiếp theo cho danh mục tiếp theo.
            } else {
                // Ghi thông tin danh mục thông thường (không chứa " - ").
                XSSFRow row = sheet.createRow(rowNum);
                String nameWithCount = category.getName() + " (" + category.getQuestionCount() + ")";
                row.createCell(columnIndex).setCellValue(nameWithCount);

                // Tính tổng số câu hỏi của danh mục cha và các danh mục con.
                int totalQuestionCount = getTotalQuestionCount(category);
                row.createCell(columnIndex + 1).setCellValue(totalQuestionCount);

                rowNum++; // Xuống dòng.
            }

            //         Nếu danh mục có danh mục con.
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                boolean allChildrenAreLeaf = category.getChildren().stream()
                        .allMatch(child -> child.getChildren() == null || child.getChildren().isEmpty());

                if (allChildrenAreLeaf) {
                    // Ghi tất cả danh mục con trên cùng một hàng ngang dưới danh mục cha.
                    XSSFRow childRow = sheet.createRow(rowNum);
                    int childColumn = columnIndex + 1;
                    for (QuestionCategoriesDTO child : category.getChildren()) {
                        int childNameWithCount = child.getQuestionCount();
                        childRow.createCell(childColumn).setCellValue(childNameWithCount);
                        childColumn++;
                    }
                    rowNum++;
                } else {
                    // Gọi đệ quy cho từng danh mục con nếu có danh mục con lồng.
                    for (QuestionCategoriesDTO child : category.getChildren()) {
                        rowNum = exportCategoriesToExcel(List.of(child), sheet, rowNum, columnIndex + 1);
                    }
                }
            }
        }

        return rowNum;
    }


    private int getTotalQuestionCount(QuestionCategoriesDTO category) {
        int total = category.getQuestionCount();
        if (category.getChildren() != null) {
            for (QuestionCategoriesDTO child : category.getChildren()) {
                total += getTotalQuestionCount(child);
            }
        }
        return total;
    }
}
