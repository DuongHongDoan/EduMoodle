package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.QuizAttemptListDTO;
import com.example.edumoodle.DTO.UsersDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuizService {

    @Autowired
    private UsersService usersService;

    @Value("${moodle.token}")
    private String token;

    @Value("${moodle.domainName}")
    private String domainName;

    private final RestTemplate restTemplate;
    public QuizService() {
        this.restTemplate = new RestTemplate();
    }

    //lấy thông tin chi tiết của một bài thi
    public List<QuizAttemptListDTO.AttemptDTO> getAllAttemptStudents(Integer quizId, Integer courseId) {
        String apiMoodleFunc = "mod_quiz_get_user_attempts";
        String apiReviewFunc = "mod_quiz_get_attempt_review";

        List<QuizAttemptListDTO.AttemptDTO> allAttempts = new ArrayList<>();
        //lấy danh sách id của sv đã đăng ký vào course
        List<UsersDTO> usersEnrolled = usersService.getEnrolledUsers(courseId);
        List<UsersDTO> studentsEnrolled = usersEnrolled.stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.getRoleid() == 5))
                .toList();
        List<Integer> userIds = new ArrayList<>();
        for (UsersDTO user : studentsEnrolled) {
            userIds.add(user.getId());
        }
        //lặp qua ds sv đky trên --> lấy attempt của sv ấy
        for(Integer userId : userIds) {
            String url = domainName + "/webservice/rest/server.php"
                    + "?wstoken=" + token
                    + "&wsfunction=" + apiMoodleFunc
                    + "&moodlewsrestformat=json"
                    + "&quizid=" + quizId
                    + "&userid=" + userId;
            QuizAttemptListDTO response = restTemplate.getForObject(url, QuizAttemptListDTO.class);
            if(response != null && response.getAttempts() != null) {
                for(QuizAttemptListDTO.AttemptDTO attempt : response.getAttempts()) {
                    attempt.setUsersDTO(usersService.getUserByID(userId));
                    //tính thời gian làm bài
                    QuizAttemptListDTO.AttemptDTO durationAttempt = new QuizAttemptListDTO.AttemptDTO(attempt.getTimestart(), attempt.getTimefinish());
                    attempt.setDuration(durationAttempt.getDurationFormat());
                    // Lấy điểm chi tiết từ API mod_quiz_get_attempt_review
                    String reviewUrl = domainName + "/webservice/rest/server.php"
                            + "?wstoken=" + token
                            + "&wsfunction=" + apiReviewFunc
                            + "&moodlewsrestformat=json"
                            + "&attemptid=" + attempt.getId();
                    QuizAttemptListDTO reviewResponse = restTemplate.getForObject(reviewUrl, QuizAttemptListDTO.class);

                    if (reviewResponse != null && reviewResponse.getGrade() != null) {
                        attempt.setGrade(reviewResponse.getGrade());
                    }
                }
                allAttempts.addAll(response.getAttempts());
            }
        }
        return allAttempts;
    }
}
