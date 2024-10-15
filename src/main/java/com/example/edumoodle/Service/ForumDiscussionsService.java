package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.ForumDiscussionsDTO;
import com.example.edumoodle.DTO.SectionsDTO;
import com.example.edumoodle.DTO.Sections_ModuleDTO;
import com.example.edumoodle.DTO.UsersDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class ForumDiscussionsService {
    @Value("${moodle.token}")
    private String token;

    @Value("${moodle.domainName}")
    private String domainName;

    private final RestTemplate restTemplate;
    public ForumDiscussionsService() {
        this.restTemplate = new RestTemplate();
    }

    public String getForumName(Integer courseId, Integer forumId) {
        String apiMoodleFunc = "core_course_get_contents";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json"
                + "&courseid=" + courseId;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<SectionsDTO[]> response = restTemplate.getForEntity(url, SectionsDTO[].class);
        SectionsDTO[] sections = response.getBody();

        String forumName = "";
        for(int i = 0; i< Objects.requireNonNull(sections).length; i++) {
            List<Sections_ModuleDTO> sectionModules = sections[i].getModules();
            for (Sections_ModuleDTO module : sectionModules) {
                // Kiểm tra xem instance của module có khớp với forumId không
                if (Objects.equals(module.getInstance(), forumId)) {
                    forumName = module.getName();
                    break;
                }
            }
        }
        System.out.println("Tên forum: " + forumName);
        return forumName;
    }

    public List<ForumDiscussionsDTO> getForumDiscussions(Integer forumId) {
        String apiMoodleFunc = "mod_forum_get_forum_discussions";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json"
                + "&forumid=" + forumId;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String jsonResponse = response.getBody();

        // Chuyển đổi JSON trả về thành List<ForumDiscussionsDTO>
        List<ForumDiscussionsDTO> discussionsList = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode discussionsNode = rootNode.path("discussions");
            if (discussionsNode.isArray()) {
                discussionsList = objectMapper.readValue(discussionsNode.toString(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ForumDiscussionsDTO.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return discussionsList;
    }
}
