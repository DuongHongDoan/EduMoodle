package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.UsersDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class UsersService {

    @Value("${moodle.token}")
    private String token;

    @Value("${moodle.domainName}")
    private String domainName;

    private final RestTemplate restTemplate;
    public UsersService() {
        this.restTemplate = new RestTemplate();
    }

    // Lấy danh sách giảng viên và sinh viên của khóa học
    public List<UsersDTO> getEnrolledUsers(int courseId) {
        String apiMoodleFunc = "core_enrol_get_enrolled_users";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json"
                + "&courseid=" + courseId;

        ResponseEntity<UsersDTO[]> response = restTemplate.getForEntity(url, UsersDTO[].class);
        UsersDTO[] enrolledUsers = response.getBody();

        assert enrolledUsers != null;
        return Arrays.asList(enrolledUsers);
    }

    //lấy danh sách tất cả user được thêm vào moodle (apiMoodleFunc là plugin import vào dự án moodle)
    public List<UsersDTO> getAllUsers() {
        String apiMoodleFunc = "local_getusers_get_users";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json";

        ResponseEntity<UsersDTO[]> response = restTemplate.getForEntity(url, UsersDTO[].class);
        UsersDTO[] usersList = response.getBody();

        assert usersList != null;
        return Arrays.asList(usersList);
    }
}
