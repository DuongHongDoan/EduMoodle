package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.*;
import com.example.edumoodle.Model.QuestionAnswersEntity;
import com.example.edumoodle.Model.QuestionsEntity;
import com.example.edumoodle.Repository.QuestionsRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

@Service
public class QuestionsService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionCategoriesService.class);

    @Value("${moodle.token}")
    private String token;

    @Value("${moodle.domainName}")
    private String domainName;

    @Autowired
//    private RestTemplate restTemplate;
    private final RestTemplate restTemplate;
    private final String MOODLE_API_URL =
            "http://localhost/moodle/webservice/rest/server.php"
                    + "?wstoken=1e9f4e6a7041b1d5badeeda8e183df5c"
                    + "&wsfunction=local_question_get_question_by_category"
                    + "&moodlewsrestformat=json"
                    + "&questioncategoryid=";

    @Autowired
    public QuestionsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

//    public QuestionsResponseDTO getQuestionsByCategory(Long categoryId) {
//        String url = MOODLE_API_URL + categoryId;
//
//        System.out.println(url);
//        return restTemplate.getForObject(url, QuestionsResponseDTO.class);
//    }

    public QuestionsResponseDTO getQuestionsByCategory(Long categoryId) {
        String url = MOODLE_API_URL + categoryId;

        System.out.println(url);
        try {
            return restTemplate.getForObject(url, QuestionsResponseDTO.class);
        } catch (RestClientException e) {
            // Xử lý lỗi và trả về null hoặc ném ngoại lệ
            System.err.println("Lỗi khi gọi API Moodle: " + e.getMessage());
            return null;
        }
    }




//    public QuestionAnswersResponseDTO getAnswersByQuestionId(int questionId) {
//        String apiMoodleFunc = "local_question_get_answers";
//        String url = UriComponentsBuilder.fromHttpUrl(domainName)
//                .queryParam("wstoken", token)
//                .queryParam("wsfunction", apiMoodleFunc)
//                .queryParam("moodlewsrestformat", "json")
//                .queryParam("questionid", questionId)
//                .toUriString();
//
//        // Gửi yêu cầu và nhận phản hồi từ Moodle
//        String responseBody = restTemplate.getForObject(url, String.class);
//
//        // Phân tích phản hồi
//        List<QuestionAnswersDTO> answers = parseResponse(responseBody);
//
//        // Trả về phản hồi
//        return new QuestionAnswersResponseDTO("success", answers);
//    }
//
//    private List<QuestionAnswersDTO> parseResponse(String responseBody) {
//        List<QuestionAnswersDTO> answers = new ArrayList<>();
//
//        // Phân tích JSON phản hồi
//        JSONObject jsonResponse = new JSONObject(responseBody);
//        JSONArray jsonAnswers = jsonResponse.getJSONArray("answers");
//
//        for (int i = 0; i < jsonAnswers.length(); i++) {
//            JSONObject jsonAnswer = jsonAnswers.getJSONObject(i);
//            QuestionAnswersDTO dto = new QuestionAnswersDTO();
//            dto.setId(jsonAnswer.getInt("id"));
//            dto.setAnswerText(jsonAnswer.getString("answertext"));
//            dto.setCorrect(jsonAnswer.getBoolean("correct"));
//
//            answers.add(dto);
//        }
//
//        return answers;
//    }

    public void addQuestionToMoodle(QuestionsDTO questionsDTO) {
        String apiUrl = "http://localhost/moodle/webservice/rest/server.php?wstoken=1e9f4e6a7041b1d5badeeda8e183df5c&wsfunction=local_question_add_question&moodlewsrestformat=json";

        // Tạo payload cho API
        String urlWithParams = String.format("%s&category_id=%d&name=%s&questiontext=%s&qtype=%s&correctanswerindex=%d",
                apiUrl,
                questionsDTO.getCategoryId(),
                questionsDTO.getName(),
                questionsDTO.getQuestionText(),
                questionsDTO.getQtype(),
                questionsDTO.getCorrectAnswerIndex());

        // Thêm các đáp án vào URL
        for (int i = 0; i < questionsDTO.getAnswers().size(); i++) {
            urlWithParams += String.format("&answers[%d]=%s", i, questionsDTO.getAnswers().get(i).getAnswerText());
        }

        // Gọi API
        restTemplate.postForEntity(urlWithParams, null, String.class);
    }
    private final String MOODLE_URL = "http://localhost/moodle/webservice/rest/server.php";
    private final String TOKEN = "1e9f4e6a7041b1d5badeeda8e183df5c";

    public String addQuestion(QuestionsDTO questionDto) {
        RestTemplate restTemplate = new RestTemplate();

        // Chuyển đổi danh sách đáp án thành chuỗi query
        StringBuilder answersQuery = new StringBuilder();
        for (int i = 0; i < questionDto.getAnswers().size(); i++) {
            answersQuery.append(String.format("&answers[%d]=%s", i, questionDto.getAnswers().get(i).getAnswerText()));
        }

        String url = String.format("%s?wstoken=%s&wsfunction=local_question_add_question&moodlewsrestformat=json&category_id=%d&name=%s&questiontext=%s&qtype=%s&correctanswerindex=%d%s",
                MOODLE_URL, TOKEN, questionDto.getCategoryId(), questionDto.getName(),
                questionDto.getQuestionText(), questionDto.getQtype(), questionDto.getCorrectAnswerIndex(),
                answersQuery.toString());

        // Gửi yêu cầu HTTP POST đến Moodle API
        return restTemplate.postForObject(url, null, String.class);
    }
}
