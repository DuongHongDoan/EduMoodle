package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.*;
import com.example.edumoodle.Model.QuestionAnswersEntity;
import com.example.edumoodle.Model.QuestionCategoriesEntity;
import com.example.edumoodle.Model.QuestionsEntity;
import com.example.edumoodle.Repository.QuestionAnswersRepository;
import com.example.edumoodle.Repository.QuestionCategoriesRepository;
import com.example.edumoodle.Repository.QuestionsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service

public class QuestionsService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionCategoriesService.class);
    @Value("${moodle.token}")
    private String token;
    @Value("${moodle.domainName}")
    private String domainName;
    @Autowired

    private final RestTemplate restTemplate;

    @Autowired
    public QuestionsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    private com.example.edumoodle.Repository.QuestionCategoriesRepository QuestionCategoriesRepository;
    @Autowired
    private QuestionsRepository QuestionsRepository;
    @Autowired
    private QuestionAnswersRepository QuestionAnswersRepository;

    public QuestionsResponseDTO getQuestionsByCategory(Long categoryId) {
        // Định nghĩa tên hàm API của Moodle
        String apiMoodleFunc = "local_question_get_question_by_category";

        // Tạo URL động bằng các biến `domainName`, `token`, và `apiMoodleFunc`
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json"
                + "&questioncategoryid=" + categoryId;

        System.out.println("URL nè: " + url);
        try {
            QuestionsResponseDTO response = restTemplate.getForObject(url, QuestionsResponseDTO.class);

            // Kiểm tra nếu response không null và có danh sách câu hỏi
            if (response != null && response.getQuestions() != null) {
                int questionCount = response.getQuestions().size();
                System.out.println("Số lượng câu hỏi: " + questionCount);
            } else {
                System.out.println("Không có câu hỏi nào trong danh mục này.");
            }

            return response;
        } catch (RestClientException e) {
            // Xử lý lỗi và trả về null hoặc ném ngoại lệ
            System.err.println("Lỗi khi gọi API Moodle: " + e.getMessage());
            return null;
        }
    }

    private void addQuestionToWebDatabase(QuestionsDTO questionsDTO) {
        // Lấy thông tin danh mục câu hỏi từ cơ sở dữ liệu
        QuestionCategoriesEntity categoryEntity = QuestionCategoriesRepository.findByMoodleId(questionsDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category ID không tồn tại"));

        // Tạo đối tượng `QuestionsEntity` từ DTO
        QuestionsEntity questionEntity = new QuestionsEntity();
        questionEntity.setCategoryId(categoryEntity);
        questionEntity.setName(questionsDTO.getName());
        questionEntity.setQuestionText(questionsDTO.getQuestionText());

        // Kiểm tra và thiết lập giá trị qtype
        if (questionsDTO.getQtype() == null || questionsDTO.getQtype().isEmpty()) {
            questionsDTO.setQtype("multichoice"); // Mặc định là câu hỏi trắc nghiệm
        }
        questionEntity.setQtype(questionsDTO.getQtype());

        // Lưu Moodle ID vào cơ sở dữ liệu web
        questionEntity.setMoodleId(questionsDTO.getMoodleId());

        // Thêm các câu trả lời vào `QuestionsEntity`
        if (questionsDTO.getAnswers() != null) {
            int correctIndex = questionsDTO.getCorrectAnswerIndex(); // Lấy chỉ số đáp án đúng
            for (int i = 0; i < questionsDTO.getAnswers().size(); i++) {
                QuestionAnswersDTO answerDTO = questionsDTO.getAnswers().get(i);

                String answerText = answerDTO.getAnswerText();
                if (answerText == null || answerText.trim().isEmpty()) {
                    System.out.println("Bỏ qua đáp án rỗng hoặc không hợp lệ.");
                    continue; // Bỏ qua đáp án không hợp lệ
                }

                QuestionAnswersEntity answerEntity = new QuestionAnswersEntity();
                answerEntity.setAnswerText(answerText);
                answerEntity.setCorrect(i == correctIndex); // Đặt `correct = 1` nếu đáp án là đúng
                answerEntity.setQuestion(questionEntity); // Liên kết câu trả lời với câu hỏi
                questionEntity.addAnswer(answerEntity);
            }
        }

        // Lưu `QuestionsEntity` vào cơ sở dữ liệu
        QuestionsRepository.save(questionEntity);
        System.out.println("Thêm câu hỏi vào cơ sở dữ liệu web thành công.");
    }

    public void importQuestionsFromTxt(String filePath, int categoryId) {
        List<QuestionsDTO> questionsList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            QuestionsDTO currentQuestion = null;
            List<QuestionAnswersDTO> answers = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                } else if (line.startsWith("ANSWER:")) {
                    if (currentQuestion != null) {
                        // Set correct answer index
                        String correctAnswer = line.substring(7).trim(); // Ví dụ: "A"
                        for (int i = 0; i < answers.size(); i++) {
                            if (answers.get(i).getAnswerText().startsWith(correctAnswer + ".")) {
                                currentQuestion.setCorrectAnswerIndex(i);
                                break;
                            }
                        }

                        // Nếu questionText rỗng, dùng name làm fallback
                        if (currentQuestion.getQuestionText() == null || currentQuestion.getQuestionText().isEmpty()) {
                            currentQuestion.setQuestionText(currentQuestion.getName());
                        }

                        // Thêm câu trả lời vào câu hỏi
                        currentQuestion.setAnswers(new ArrayList<>(answers));
                        questionsList.add(currentQuestion);
                    }

                    // Reset cho câu hỏi tiếp theo
                    currentQuestion = null;
                    answers = new ArrayList<>();
                } else if (line.matches("^[A-D]\\..*")) {
                    // Dòng chứa đáp án (A., B., ...)
                    answers.add(new QuestionAnswersDTO(line));
                } else {
                    // Các dòng khác là phần thân câu hỏi
                    if (currentQuestion == null) {
                        currentQuestion = new QuestionsDTO();
                        currentQuestion.setCategoryId(categoryId);
                        currentQuestion.setQtype("multichoice"); // Thiết lập loại câu hỏi mặc định
                        currentQuestion.setName(line); // Sử dụng dòng đầu tiên làm tên câu hỏi
                    } else {
                        // Ghép nối phần text
                        currentQuestion.setQuestionText(
                                (currentQuestion.getQuestionText() == null ? "" : currentQuestion.getQuestionText() + " ") + line
                        );
                    }
                }
            }

            // Thêm câu hỏi cuối cùng nếu có
            if (currentQuestion != null) {
                if (currentQuestion.getQuestionText() == null || currentQuestion.getQuestionText().isEmpty()) {
                    currentQuestion.setQuestionText(currentQuestion.getName());
                }
                currentQuestion.setAnswers(new ArrayList<>(answers));
                questionsList.add(currentQuestion);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Gửi từng câu hỏi lên Moodle và lưu vào Web
        for (QuestionsDTO question : questionsList) {
            try {
                addQuestionToMoodleAndWeb(question); // Gửi câu hỏi lên Moodle và lưu vào Web
            } catch (Exception e) {
                System.err.println("Lỗi khi thêm câu hỏi: " + e.getMessage());
            }
        }
    }

    public void addQuestionToMoodleAndWeb(QuestionsDTO questionsDTO) {
        // Kiểm tra đầu vào
        if (questionsDTO == null || questionsDTO.getCategoryId() == null || questionsDTO.getName().isEmpty() || questionsDTO.getQuestionText().isEmpty()) {
            System.out.println("Thông tin câu hỏi không hợp lệ.");
            return; // Dừng việc thực hiện nếu dữ liệu không hợp lệ
        }

        String apiUrl = domainName + "/webservice/rest/server.php";
         // Đảm bảo token đúng và hợp lệ

        RestTemplate restTemplate = new RestTemplate();

        // Cấu hình tham số gửi tới Moodle API
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("wstoken", token);
        params.add("wsfunction", "local_question_add_question");
        params.add("moodlewsrestformat", "json");
        params.add("category_id", String.valueOf(questionsDTO.getCategoryId()));
        params.add("name", questionsDTO.getName());
        params.add("questiontext", questionsDTO.getQuestionText());
        params.add("qtype", "multichoice");

        // Nếu chỉ số đáp án đúng được chỉ định, thêm vào tham số
        if (questionsDTO.getCorrectAnswerIndex() != null) {
            params.add("correctanswerindex", String.valueOf(questionsDTO.getCorrectAnswerIndex()));
        }

        // Thêm các đáp án không rỗng
        if (questionsDTO.getAnswers() != null && !questionsDTO.getAnswers().isEmpty()) {
            for (int i = 0; i < questionsDTO.getAnswers().size(); i++) {
                String answerText = questionsDTO.getAnswers().get(i).getAnswerText();
                if (answerText != null && !answerText.trim().isEmpty()) {
                    params.add("answers[" + i + "]", answerText);
                }
            }
        }

        // Tạo đối tượng HTTP Entity với các tham số
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            // Gửi yêu cầu POST đến Moodle API
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());

            // Kiểm tra nếu câu hỏi đã thêm thành công vào Moodle
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                String moodleId = String.valueOf(extractMoodleId(responseBody)); // Sử dụng hàm extractMoodleId

                if (moodleId != null && !moodleId.equals("-1")) {
                    // Lưu vào cơ sở dữ liệu web
                    questionsDTO.setMoodleId(Integer.parseInt(moodleId));
                    addQuestionToWebDatabase(questionsDTO);
                    System.out.println("Câu hỏi đã được thêm thành công vào Moodle và Web.");
                } else {
                    System.out.println("Không nhận được Moodle ID từ phản hồi.");
                }
            } else {
                System.out.println("Thêm câu hỏi vào Moodle thất bại. Thông báo lỗi: " + response.getBody());
            }
        } catch (RestClientException e) {
            System.err.println("Lỗi khi thêm câu hỏi vào Moodle: " + e.getMessage());
        }
    }

    private int extractMoodleId(String jsonResponse) {
        try {
            JsonNode rootNode = new ObjectMapper().readTree(jsonResponse);

            // Kiểm tra trường "question_id" thay vì "category_id"
            if (!rootNode.has("question_id")) {
                logger.error("Phản hồi từ Moodle không có trường 'question_id'.");
                return -1;
            }

            // Trả về giá trị của "question_id"
            return rootNode.path("question_id").asInt();
        } catch (JsonProcessingException e) {
            logger.error("Lỗi khi phân tích JSON: {}", e.getMessage());
            return -1;
        }
    }

    private boolean parseDeleteResponse(String jsonResponse) {
        try {
            // Phân tích JSON phản hồi từ Moodle
            JsonNode rootNode = new ObjectMapper().readTree(jsonResponse);

            // Kiểm tra xem phản hồi có chứa thông tin xóa thành công không (tùy thuộc vào API)
            // Ví dụ: Nếu Moodle trả về "status": "success", ta coi là xóa thành công
            if (rootNode.has("status") && "success".equals(rootNode.path("status").asText())) {
                return true; // Trả về true nếu xóa thành công
            } else {
                // Xử lý trường hợp lỗi nếu Moodle không trả về status = success
                logger.error("Không thể xóa câu hỏi. Phản hồi từ Moodle: {}", jsonResponse);
                return false; // Trả về false nếu xóa không thành công
            }
        } catch (JsonProcessingException e) {
            logger.error("Lỗi khi phân tích JSON phản hồi từ Moodle: {}", e.getMessage());
            return false; // Nếu có lỗi khi phân tích JSON, trả về false
        }
    }

    //    Trong Spring, cần đảm bảo rằng phương thức xóa câu hỏi được thực thi trong một transaction.
    @Transactional
    public String deleteQuestion(int moodleId) {
        String apiMoodleFunc = "local_question_delete_question";  // API của Moodle để xóa câu hỏi
        String url = UriComponentsBuilder.fromHttpUrl(domainName + "/webservice/rest/server.php")
                .queryParam("wstoken", token)
                .queryParam("wsfunction", apiMoodleFunc)
                .queryParam("moodlewsrestformat", "json")
                .queryParam("question_id", moodleId)
                .toUriString();

        logger.info("Deleting question with ID {} from Moodle via URL: {}", moodleId, url);

        try {
            // Gửi yêu cầu xóa đến Moodle
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                boolean isDeleted = parseDeleteResponse(response.getBody());
                if (isDeleted) {
                    logger.info("Xóa câu hỏi với moodleId: {}", moodleId);
                    QuestionsRepository.deleteByMoodleId(moodleId);
                    logger.info("Câu hỏi đã bị xóa khỏi cơ sở dữ liệu.");

                    // Xóa câu hỏi trực tiếp trong cơ sở dữ liệu Web
//                    QuestionsRepository.deleteByMoodleId(moodleId);  // Đảm bảo câu hỏi được xóa khỏi web
                    return "Xóa câu hỏi thành công trên Moodle và Web.";
                } else {
                    logger.error("Moodle không xóa được câu hỏi với ID {}. Phản hồi: {}", moodleId, response.getBody());
                    return "Lỗi: Moodle không xóa được câu hỏi.";
                }
            } else {
                logger.error("Lỗi khi gọi API Moodle để xóa câu hỏi. Mã trạng thái: {}", response.getStatusCode());
                return "Lỗi: Không thể xóa câu hỏi từ Moodle. Mã trạng thái: " + response.getStatusCode();
            }
        } catch (Exception e) {
            logger.error("Lỗi khi gọi API Moodle để xóa câu hỏi: {}", e.getMessage());
            return "Lỗi: Không thể xóa câu hỏi. Chi tiết: " + e.getMessage();
        }
    }


    public QuestionsEntity getQuestionByMoodleId(int moodleId) {
        return QuestionsRepository.findByMoodleId(moodleId)
                .orElseThrow(() -> new IllegalArgumentException("Câu hỏi với Moodle ID không tồn tại"));
    }

    public void updateQuestionByMoodleId(int moodleId, QuestionsEntity question, List<String> answers, int correctAnswer) {
        System.out.println("updateQuestionByMoodleId is being called...");
        QuestionsEntity existingQuestion = getQuestionByMoodleId(moodleId);

        // Cập nhật thông tin câu hỏi
        existingQuestion.setName(question.getName());
        existingQuestion.setQuestionText(question.getQuestionText());
        existingQuestion.setQtype(question.getQtype());

        // Lấy danh sách đáp án hiện tại
        List<QuestionAnswersEntity> currentAnswers = existingQuestion.getAnswers();

        // Danh sách ID đáp án mới từ `answers`
        Set<Integer> newAnswerIds = new HashSet<>();
        for (int i = 0; i < answers.size(); i++) {
            newAnswerIds.add(i);  // Gán chỉ mục làm ID cho các đáp án mới
        }

        // Danh sách đáp án sẽ được cập nhật
        List<QuestionAnswersEntity> updatedAnswers = new ArrayList<>();

        // Cập nhật hoặc xóa đáp án
        for (Iterator<QuestionAnswersEntity> iterator = currentAnswers.iterator(); iterator.hasNext(); ) {
            QuestionAnswersEntity answer = iterator.next();
            if (newAnswerIds.contains(answer.getId())) {
                // Nếu đáp án tồn tại trong danh sách mới, cập nhật thông tin
                int answerIndex = new ArrayList<>(newAnswerIds).indexOf(answer.getId());  // Lấy chỉ mục của đáp án mới từ `newAnswerIds`
                answer.setAnswerText(answers.get(answerIndex));  // Cập nhật nội dung đáp án
                answer.setCorrect(answerIndex == correctAnswer);  // Đảm bảo đáp án đúng được xác định đúng
                updatedAnswers.add(answer);  // Thêm vào danh sách đáp án đã cập nhật
            } else {
                // Nếu đáp án không còn trong danh sách mới, xóa khỏi cơ sở dữ liệu
                iterator.remove();  // Xóa đáp án khỏi danh sách hiện tại
                QuestionAnswersRepository.delete(answer);  // Xóa đáp án khỏi cơ sở dữ liệu
            }
        }

        // Thêm đáp án mới
        for (int i = 0; i < answers.size(); i++) {
            final int index = i;  // Tạo biến final từ `i` để sử dụng trong lambda expression
            boolean isExisting = currentAnswers.stream().anyMatch(a -> a.getAnswerText().equals(answers.get(index)));
            if (!isExisting) {
                // Nếu đáp án chưa có, tạo mới đáp án
                QuestionAnswersEntity newAnswer = new QuestionAnswersEntity();
                newAnswer.setAnswerText(answers.get(index));
                newAnswer.setCorrect(index == correctAnswer);  // Đảm bảo đáp án đúng được xác định đúng
                newAnswer.setQuestion(existingQuestion);  // Gắn câu hỏi cho đáp án mới

                // Lưu đáp án mới vào cơ sở dữ liệu
                QuestionAnswersRepository.save(newAnswer);
                updatedAnswers.add(newAnswer);
            }
        }

        // Cập nhật lại danh sách đáp án
        existingQuestion.setAnswers(updatedAnswers);

        // Lưu câu hỏi và các đáp án vào cơ sở dữ liệu
        QuestionsRepository.save(existingQuestion);  // Lưu câu hỏi và đáp án vào cơ sở dữ liệu
    }

    public void updateQuestionInMoodle(QuestionsEntity question, List<String> answers, int correctAnswer) {
        System.out.println("updateQuestionInMoodle is being called...");
        System.out.println("Current qtype before sending to API: " + question.getQtype());

        // Xây dựng URL API với các tham số
        StringBuilder apiUrlBuilder = new StringBuilder(domainName + "/webservice/rest/server.php")
                .append("?wstoken=").append(token)  // Dùng token đã có sẵn
                .append("&wsfunction=local_question_update_question")
                .append("&moodlewsrestformat=json")
                .append("&qtype=").append(question.getQtype())
                .append("&name=").append(question.getName())
                .append("&questiontext=").append(question.getQuestionText())
                .append("&question_id=").append(question.getMoodleId())
                .append("&correctanswerindex=").append(correctAnswer);

        // Gửi các đáp án dưới dạng tham số API
        for (int i = 0; i < answers.size(); i++) {
            apiUrlBuilder.append("&answers[").append(i + 1).append("]=").append(answers.get(i));
        }

        // Tạo URL hoàn chỉnh
        String apiUrl = apiUrlBuilder.toString();
        System.out.println("API URL: " + apiUrl);

        // Gửi request GET (do tham số đã có trong URL)
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,  // Dùng GET thay vì POST vì các tham số được gửi qua URL
                    null,
                    String.class
            );
            System.out.println("Response status code: " + response.getStatusCode());
            System.out.println("Response body: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi yêu cầu API: " + e.getMessage());
        }
    }

}

