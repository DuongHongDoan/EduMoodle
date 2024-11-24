package com.example.edumoodle.Controller.student;

import com.example.edumoodle.DTO.ForumDiscussionsDTO;
import com.example.edumoodle.Service.ForumDiscussionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Controller
public class ForumDiscussionStudentController {
    @Autowired
    private ForumDiscussionsService forumDiscussionsService;

    @GetMapping("/student/courses/forum")
    public String getForumDiscussion(@RequestParam Integer forumId, @RequestParam Integer courseId, Model model) {
        String forumName = forumDiscussionsService.getForumName(courseId, forumId);
        model.addAttribute("forumName", forumName);

        List<ForumDiscussionsDTO> forumDiscussions = forumDiscussionsService.getForumDiscussions(forumId);
        model.addAttribute("forumDiscussions", forumDiscussions);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, 'ngày' d 'tháng' M 'năm' yyyy, h:mm a", new Locale("vi", "VN"));
        for (ForumDiscussionsDTO discussion : forumDiscussions) {
            long createdEpochSeconds = Long.parseLong(discussion.getModified());
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(createdEpochSeconds), ZoneId.systemDefault());
            String formattedDate = dateTime.format(formatter).replace("SA", "AM").replace("CH", "PM");
            ;
            discussion.setModified(formattedDate);
        }

        model.addAttribute("forumId", forumId);
        model.addAttribute("courseId", courseId);

        return "admin/Forum";
    }

}
