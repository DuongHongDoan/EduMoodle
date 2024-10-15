package com.example.edumoodle.Controller.admin;

import com.example.edumoodle.DTO.ForumDiscussionsDTO;
import com.example.edumoodle.Service.ForumDiscussionsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
@Tag(name = "Forum Discussion", description = "APIs for managing forum")
public class ForumDiscussionsController {
    @Autowired
    private ForumDiscussionsService forumDiscussionsService;

    //url = /admin/courses/forum
    @GetMapping("/courses/forum")
    public String getForumDiscussion(@RequestParam Integer forumId, @RequestParam Integer courseId, Model model) {
        String forumName = forumDiscussionsService.getForumName(courseId, forumId);
        model.addAttribute("forumName", forumName);

        List<ForumDiscussionsDTO> forumDiscussions = forumDiscussionsService.getForumDiscussions(forumId);
        model.addAttribute("forumDiscussions", forumDiscussions);

        return "admin/Forum";
    }
}
