package com.example.edumoodle.Controller.admin;

import com.example.edumoodle.Service.MyCoursesStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ResourceAdminController {

    @Autowired
    private MyCoursesStudentService myCoursesStudentService;

    // ressourse ne
    @GetMapping("admin/resource")
    public String showResource(@RequestParam("courseId") Integer moodleCourseId,
                               @RequestParam("resourceId") Integer resourceId,
                               Model model) {

        // Gọi phương thức từ service để lấy nội dung tài nguyên
        Object resourceContentObj = myCoursesStudentService.fetchModuleContent(moodleCourseId, resourceId);

        String resourceContent = "Không có dữ liệu."; // Giá trị mặc định

        // Kiểm tra nội dung tài nguyên (nếu là URL, xử lý thành thẻ <a>)
        if (resourceContentObj instanceof String) {
            String content = (String) resourceContentObj;

            // Nếu tài nguyên là URL, chuyển thành liên kết
            if (content.startsWith("http://") || content.startsWith("https://")) {
                resourceContent = "<a href='" + content + "' target='_blank'>Xem tài liệu</a>";
            } else {
                // Nếu là văn bản, giữ nguyên
                resourceContent = content;
            }
        }

        // Thêm tài nguyên vào model để truyền sang view
        model.addAttribute("resourceContent", resourceContent);

        return "student/course_resource"; // Trả về view "student/course_resource"
    }
}