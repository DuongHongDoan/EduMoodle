package com.example.edumoodle.Controller.teacher;

import com.example.edumoodle.DTO.QuestionCategoriesDTO;
import com.example.edumoodle.Model.QuestionCategoriesEntity;
import com.example.edumoodle.Service.QuestionCategoriesService;
import com.opencsv.CSVReader;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


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
            return "redirect:/teacher/courses/view/Add_QuestionCategories?courseId=" + courseId;
        }

        // Nếu thêm thành công, gửi thông báo thành công
        redirectAttributes.addFlashAttribute("response", "Danh mục đã được thêm thành công!");
        return "redirect:/teacher/courses/view/list_question?courseId=" + courseId;
    }
//    import file Excel
    @PostMapping("/uploadExcel")
    public String uploadExcelFile(@RequestParam("file") MultipartFile file,
                                  @RequestParam("courseId") int courseId,
                                  RedirectAttributes redirectAttributes) {
        String response = categoriesService.importCategoriesFromExcel(file, courseId);

        // Thêm thông báo thành công hoặc lỗi
        if (response.contains("Lỗi")) {
            redirectAttributes.addFlashAttribute("error", response);
        } else {
            redirectAttributes.addFlashAttribute("response", response);
        }

        return "redirect:/teacher/courses/view/list_question?courseId=" + courseId;
    }
//    Xóa danh mục
    @PostMapping("/teacher/courses/view/Delete_QuestionCategory/{moodle_id}")
    public String deleteCategory(@PathVariable Integer moodle_id, @RequestParam("courseId") int courseId, Model model) {
        try {
            // Xử lý xóa danh mục
            categoriesService.deleteCategory(moodle_id);
            // Thêm thông báo thành công vào model
            model.addAttribute("message", "Xóa danh mục thành công!");
        } catch (RuntimeException e) {
            // Thêm thông báo lỗi vào model
            model.addAttribute("error", "Lỗi: " + e.getMessage());
        }

        // Redirect lại trang danh sách với courseId
        return "redirect:/teacher/courses/view/list_question?courseId=" + courseId;
    }

    // Hiển thị form sửa danh mục câu hỏi
//    @GetMapping("/teacher/courses/view/update/{moodleId}")
//    public String listQuestions(@RequestParam("courseId") int courseId, Model model) {
//        // Lấy danh sách câu hỏi theo courseId
//        List<QuestionCategoriesDTO> categories = categoriesService.getQuestionCategories(courseId);
//        model.addAttribute("questionCategories", categories);
//        model.addAttribute("courseId", courseId);
//        return "teacher/list_question";  // Chuyển hướng đến trang danh sách câu hỏi
//    }
//    @GetMapping("/teacher/courses/view/update/{moodleId}")
//    public String editcategoryQuestions(@PathVariable("moodleId") int moodleId, @RequestParam("courseId") int courseId, Model model) {
//        List<QuestionCategoriesDTO> categories = categoriesService.getQuestionCategories(courseId);
//        model.addAttribute("questionCategories", categories);
//        model.addAttribute("courseId", courseId);
//        model.addAttribute("moodleId", moodleId);
//        return "teacher/Edit_QuestionCategory";  // Chuyển hướng đến trang chỉnh sửa
//    }
//    @GetMapping("/teacher/courses/view/update")
//    public String editCategory(@PathVariable("moodleId") int moodleId,
//                               @RequestParam("courseId") int courseId,
//                               Model model) {
//        // Chuyển tiếp đến trang chỉnh sửa danh mục câu hỏi
//        System.out.println("Moodle ID from GET: " + moodleId);
//        System.out.println("Course ID from GET: " + courseId);
//        model.addAttribute("moodleId", moodleId);
//        model.addAttribute("courseId", courseId);
//        return "teacher/Edit_QuestionCategory";  // Trang chỉnh sửa danh mục câu hỏi
//    }
    // This is your GET method, which works fine
//    @GetMapping("/teacher/courses/view/update/{moodleId}")
//    public String editCategory(@PathVariable("moodleId") int moodleId,
//                               @RequestParam("courseId") int courseId,
//                               Model model) {
//        // Chuyển tiếp đến trang chỉnh sửa danh mục câu hỏi
//        System.out.println("Moodle ID from GET: " + moodleId);
//        System.out.println("Course ID from GET: " + courseId);
//        model.addAttribute("moodleId", moodleId);
//        model.addAttribute("courseId", courseId);
//        return "teacher/Edit_QuestionCategory";  // Trang chỉnh sửa danh mục câu hỏi
//    }

    @GetMapping("/teacher/courses/view/update/{moodleId}")
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
        if (response != null && response.containsKey("categories")) {
            List<Map<String, Object>> categories = (List<Map<String, Object>>) response.get("categories");

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
            redirectAttributes.addFlashAttribute("message", message);
        } else {
            redirectAttributes.addFlashAttribute("error", message);
        }

        // Quay lại trang danh sách câu hỏi
        return "redirect:/teacher/courses/view/list_question?courseId=" + courseId;
    }

//
//    @PostMapping("/teacher/courses/view/update")
//    public String updateCategory(@RequestParam("moodleId") int moodleId,
//                                 @RequestParam("courseId") int courseId,
//                                 @RequestParam("name") String name,
//                                 @RequestParam("contextId") int contextId,
//                                 @RequestParam("info") String info,
//                                 @RequestParam("parent") int parent,
//                                 RedirectAttributes redirectAttributes) {
//        // Ghi log để kiểm tra tất cả các tham số
//        System.out.println("Updating category - moodleId: " + moodleId + ", courseId: " + courseId + ", name: " + name + ", contextId: " + contextId + ", info: " + info + ", parent: " + parent);
//
//        // Cập nhật danh mục
//        String result = categoriesService.updateCategory(moodleId, name, contextId, info, parent);
//
//        if (result != null) {
//            redirectAttributes.addFlashAttribute("message", "Cập nhật danh mục thành công!");
//        } else {
//            redirectAttributes.addFlashAttribute("error", "Cập nhật danh mục thất bại!");
//        }
//
//        return "redirect:/teacher/courses/view/list_question?courseId=" + courseId;
//    }




}
