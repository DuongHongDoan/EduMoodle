package com.example.edumoodle.Controller.teacher;

//import ch.qos.logback.core.model.Model;
import com.example.edumoodle.DTO.*;
import com.example.edumoodle.Model.QuestionAnswersEntity;
import com.example.edumoodle.Model.QuestionsEntity;
import com.example.edumoodle.Repository.QuestionsRepository;
import com.example.edumoodle.Service.QuestionCategoriesService;
import jakarta.validation.Valid;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.ui.Model;
import com.example.edumoodle.Repository.QuestionCategoriesRepository;
import com.example.edumoodle.Repository.UserRoleRepository;
import com.example.edumoodle.Service.QuestionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jdk.jfr.Category;
import org.aspectj.weaver.patterns.TypePatternQuestions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class QuestionsController {
    private  QuestionsService questionsService;
    @Autowired
    private QuestionCategoriesService categoriesService;



    @Autowired
    public QuestionsController(QuestionsService questionsService) {
        this.questionsService = questionsService;
    }

    @GetMapping("/teacher/courses/view/categories/{categoryId}/questions")
    public String getQuestionsByCategory(@PathVariable Long categoryId, Model model) {
        // Gọi service để lấy danh sách câu hỏi
        QuestionsResponseDTO response = questionsService.getQuestionsByCategory(categoryId);

        // Đưa danh sách tên câu hỏi vào model để hiển thị trên view
        if (response != null && response.getQuestions() != null) {
            model.addAttribute("questions", response.getQuestions());
        } else {
            model.addAttribute("error", "Không có câu hỏi nào.");
        }

        return "teacher/questions"; // Trả về trang 'questions.html'
    }



    private static final Logger logger = LoggerFactory.getLogger(QuestionsController.class);


    @GetMapping("/teacher/courses/view/categories/{categoryId}/add-question")
    public String showAddQuestionForm(@PathVariable("categoryId") Integer categoryId, Model model) {
        // Giả sử bạn có một service để lấy danh sách các danh mục câu hỏi
    //    List<QuestionCategoriesDTO> categories = categoriesService.getQuestionCategories();
    //    model.addAttribute("categories", categories);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("questionsDTO", new QuestionsDTO());
        return "teacher/Add_question";  // Đảm bảo tên trang HTML đúng
    }


@PostMapping("/api/questions/add")
public String addQuestion(@RequestParam("categoryId") Integer categoryId,
                          @RequestParam("name") String name,
                          @RequestParam("questionText") String questionText,
                          @RequestParam("qtype") String qtype,
                          @RequestParam("correctanswerindex") Integer correct,
                          @RequestParam("answers") List<String> answers,
                          RedirectAttributes redirectAttributes) {

    if (answers.size() < 4) {
        redirectAttributes.addFlashAttribute("error", "Vui lòng cung cấp đầy đủ 4 đáp án.");
        return "redirect:/teacher/courses/view/categories/" + categoryId + "/questions";
    }

    try {
        QuestionsDTO questionsDTO = new QuestionsDTO();
        questionsDTO.setCategoryId(categoryId);
        questionsDTO.setName(name);
        questionsDTO.setQuestionText(questionText);
        questionsDTO.setQtype(qtype);
        questionsDTO.setCorrectAnswerIndex(correct);

        List<QuestionAnswersDTO> answerList = new ArrayList<>();
        for (int i = 0; i < answers.size(); i++) {
            QuestionAnswersDTO answer = new QuestionAnswersDTO();
            answer.setAnswerText(answers.get(i));
            answer.setCorrect(i == correct);
            answerList.add(answer);
        }
        questionsDTO.setAnswers(answerList);

        questionsService.addQuestionToMoodleAndWeb(questionsDTO);
        redirectAttributes.addFlashAttribute("message", "Thêm câu hỏi thành công!");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm câu hỏi: " + e.getMessage());
    }

    return "redirect:/teacher/courses/view/categories/" + categoryId + "/questions";
}

@PostMapping("/api/questions/import")
public String importQuestions(@RequestParam("file") MultipartFile file,
                              @RequestParam("categoryId") int categoryId,
                              RedirectAttributes redirectAttributes) {

    if (file.isEmpty() || !file.getOriginalFilename().endsWith(".txt")) {
        redirectAttributes.addFlashAttribute("error", "Vui lòng tải lên tệp .txt hợp lệ.");
        return "redirect:/teacher/courses/view/categories/" + categoryId + "/questions";
    }

//    File tempFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
    try {
        // Lưu file tạm thời để xử lý
        String filePath = System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename();
        File tempFile = new File(filePath);
        file.transferTo(tempFile);

        // Gọi service để import câu hỏi từ file
        questionsService.importQuestionsFromTxt(filePath, categoryId);
        redirectAttributes.addFlashAttribute("message", "Import câu hỏi từ file thành công!");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi khi import câu hỏi: " + e.getMessage());
        e.printStackTrace();
    }

    return "redirect:/teacher/courses/view/categories/" + categoryId + "/questions";

}


    @PostMapping("/teacher/questions/delete/{moodle_id}")
public String deleteQuestion(@PathVariable Integer moodle_id,
                             @RequestParam("categoryId") int categoryId,
                             RedirectAttributes redirectAttributes) {
    try {
        // Xử lý xóa câu hỏi
        questionsService.deleteQuestion(moodle_id);
        // Thêm thông báo thành công vào RedirectAttributes
        redirectAttributes.addFlashAttribute("response", "Xóa câu hỏi thành công!");
    } catch (RuntimeException e) {
        // Thêm thông báo lỗi vào RedirectAttributes
        redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
    }

    // Redirect về trang danh sách câu hỏi trong danh mục
    return "redirect:/teacher/courses/view/categories/" + categoryId + "/questions";
}


    @GetMapping("/teacher/questions/preview/{id}")

    public String previewQuestion(@PathVariable("id") int id, Model model) {
        QuestionsEntity question = questionsService.getQuestionByMoodleId(id);

        if (question != null) {
            // Kiểm tra dữ liệu
            System.out.println(question.getName()); // In ra tên câu hỏi
            System.out.println(question.getQtype()); // In ra loại câu hỏi
            for (QuestionAnswersEntity answer : question.getAnswers()) {
                System.out.println(answer.getAnswerText()); // In ra từng đáp án
            }
        }

        model.addAttribute("question", question);
        return "teacher/preview";
    }

@GetMapping("teacher/edit-question/{moodleId}")
public String editQuestion(@PathVariable int moodleId, Model model) {
    // Lấy câu hỏi từ Moodle ID
    QuestionsEntity question = questionsService.getQuestionByMoodleId(moodleId);
    List<QuestionAnswersEntity> answers = question.getAnswers();

    // Lấy categoryId từ câu hỏi, vì category là một đối tượng
    int categoryId = question.getCategoryId().getMoodleId(); // Duyệt qua category để lấy ID

    // Thêm câu hỏi, danh sách đáp án và categoryId vào model
    model.addAttribute("question", question);
    model.addAttribute("answers", answers);
    model.addAttribute("categoryId", categoryId);  // Truyền categoryId vào model

    // Nếu bạn muốn chọn đáp án đúng từ danh sách trả về
    model.addAttribute("correctAnswer", answers.stream()
            .filter(a -> a.correct())
            .findFirst()
            .orElse(null)); // Nếu không có đáp án đúng, trả về null

    return "teacher/Edit_Questions";
}

    @PostMapping("teacher/edit-question/{moodleId}")
    public String updateQuestion(@PathVariable int moodleId,
                                 @RequestParam String name,
                                 @RequestParam String questionText,
                                 @RequestParam List<String> answers,
                                 @RequestParam int correctAnswer,
                                 @RequestParam(defaultValue = "0") int categoryId,
                                 RedirectAttributes redirectAttributes) {
        try {
            QuestionsEntity question = questionsService.getQuestionByMoodleId(moodleId);
            question.setName(name);
            question.setQuestionText(questionText);
            System.out.println("updateQuestionByMoodleId is being called...");

            // Cập nhật câu hỏi và các đáp án
            questionsService.updateQuestionInMoodle(question, answers, correctAnswer);
            questionsService.updateQuestionByMoodleId(moodleId, question, answers, correctAnswer);

            // Thêm thông báo thành công
            redirectAttributes.addFlashAttribute("message", "Cập nhật câu hỏi thành công.");

            // Đảm bảo categoryId không bị null và trả về đúng trang danh sách câu hỏi
            redirectAttributes.addFlashAttribute("categoryId", categoryId);
            return "redirect:/teacher/courses/view/categories/" + categoryId + "/questions"; // Đảm bảo categoryId chính xác
        } catch (Exception e) {
            // Nếu có lỗi, thông báo lỗi
//            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật câu hỏi: " + e.getMessage());
            return "redirect:/teacher/courses/view/categories/" + categoryId + "/questions";
        }
    }



}
