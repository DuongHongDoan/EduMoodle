package com.example.edumoodle.Controller.teacher;

//import ch.qos.logback.core.model.Model;
import com.example.edumoodle.DTO.QuestionAnswersDTO;
import com.example.edumoodle.DTO.QuestionAnswersResponseDTO;
import com.example.edumoodle.DTO.QuestionsDTO;
import com.example.edumoodle.Model.QuestionsEntity;
import com.example.edumoodle.Repository.QuestionsRepository;
import com.example.edumoodle.Service.QuestionCategoriesService;
import jakarta.validation.Valid;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.ui.Model;
import com.example.edumoodle.DTO.QuestionsResponseDTO;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class QuestionsController {
    private final QuestionsService questionsService;

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
    public String showAddQuestionForm(@PathVariable Integer categoryId, Model model) {
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("questionsDTO", new QuestionsDTO());
        return "teacher/Add_questions";
    }

    @PostMapping("/api/questions/add")
    public ResponseEntity<?> addQuestion(@RequestParam("categoryId") Integer categoryId,
                                         @RequestParam("name") String name,
                                         @RequestParam("questionText") String questionText,
                                         @RequestParam("qtype") String qtype) {
        // Xử lý logic để thêm câu hỏi vào cơ sở dữ liệu
        return ResponseEntity.ok("Question added successfully");
    }


}
