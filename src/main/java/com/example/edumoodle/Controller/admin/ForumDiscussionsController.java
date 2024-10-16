package com.example.edumoodle.Controller.admin;

import com.example.edumoodle.DTO.ForumDiscussionsDTO;
import com.example.edumoodle.Service.ForumDiscussionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/admin")
@Tag(name = "Forum Discussion", description = "APIs for managing forum")
public class ForumDiscussionsController {
    @Autowired
    private ForumDiscussionsService forumDiscussionsService;

    //url = /admin/courses/forum?
    @GetMapping("/courses/forum")
    public String getForumDiscussion(@RequestParam Integer forumId, @RequestParam Integer courseId, Model model) {
        String forumName = forumDiscussionsService.getForumName(courseId, forumId);
        model.addAttribute("forumName", forumName);

        List<ForumDiscussionsDTO> forumDiscussions = forumDiscussionsService.getForumDiscussions(forumId);
        model.addAttribute("forumDiscussions", forumDiscussions);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, 'ngày' d 'tháng' M 'năm' yyyy, h:mm a", new Locale("vi", "VN"));
        for (ForumDiscussionsDTO discussion : forumDiscussions) {
            long createdEpochSeconds = Long.parseLong(discussion.getModified());
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(createdEpochSeconds), ZoneId.systemDefault());
            String formattedDate = dateTime.format(formatter).replace("SA", "AM").replace("CH", "PM");;
            discussion.setModified(formattedDate);
        }

        model.addAttribute("forumId", forumId);
        model.addAttribute("courseId", courseId);

        return "admin/Forum";
    }

    @Operation(summary = "Add discussion", description = "add new discussion")
    @ApiResponse(responseCode = "200", description = "Successfully add discussion")
    //    url = /admin/courses/add-discussion?
    @PostMapping("/courses/add-discussion")
    public String addNewDiscussion(@RequestParam Integer forumId, @RequestParam String subject, @RequestParam String message,
                                   @RequestParam Integer courseId, Model model) {
        forumDiscussionsService.addNewDiscussion(forumId, subject, message);
        return "redirect:/admin/courses/forum?courseId=" + courseId + "&forumId=" + forumId;
    }

    // url = /admin/courses/edit-discussion?
    @GetMapping("/courses/edit-discussion")
    public String getFormEditDiscussion(@RequestParam Integer postId, @RequestParam Integer courseId,
                                        @RequestParam Integer forumId, Model model) {
        ForumDiscussionsDTO forumDiscussion = forumDiscussionsService.getPostById(postId);

        if (forumDiscussion != null) {
            model.addAttribute("subject", forumDiscussion.getSubject());
            model.addAttribute("message", forumDiscussion.getMessage());
            model.addAttribute("postId", postId);
            model.addAttribute("courseId", courseId);
            model.addAttribute("forumId", forumId);
        } else {
            // Nếu không tìm thấy bài viết, có thể thêm thông báo lỗi hoặc xử lý theo ý muốn
            model.addAttribute("errorMessage", "Không tìm thấy bài viết.");
        }

        return "admin/EditDiscussPost";
    }

    @Operation(summary = "Edit discussion", description = "edit discussion")
    @ApiResponse(responseCode = "200", description = "Successfully edited discussion")
    //    url = /admin/courses/edit-discussion?
    @PostMapping("/courses/edit-discussion")
    public String editDiscussion(@RequestParam Integer postId, @RequestParam String subject, @RequestParam String message,
                                 @RequestParam Integer courseId, @RequestParam Integer forumId,
                                 RedirectAttributes redirectAttributes) {
        forumDiscussionsService.editDiscussion(postId, subject, message);
        redirectAttributes.addFlashAttribute("successMessage", "Sửa thảo luận thành công.");

        return "redirect:/admin/courses/forum?courseId=" + courseId + "&forumId=" + forumId;
    }

    @Operation(summary = "Delete discussion", description = "delete discussion")
    @ApiResponse(responseCode = "200", description = "Successfully deleted discussion")
    //    url = /admin/courses/delete-discussion?
    @GetMapping("/courses/delete-discussion")
    public String deleteDiscussion(@RequestParam Integer postId, @RequestParam Integer courseId, @RequestParam Integer forumId,
                                   RedirectAttributes redirectAttributes) {
        forumDiscussionsService.deleteDiscussion(postId);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa thảo luận thành công.");

        return "redirect:/admin/courses/forum?courseId=" + courseId + "&forumId=" + forumId;
    }
}
