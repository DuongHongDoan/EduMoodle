package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.RecentlyAccessedCourseDTO;
import com.example.edumoodle.Model.RecentlyAccessedCoursesEntity;
import com.example.edumoodle.Repository.RecentlyAccessedCoursesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonalService {
    @Value("${moodle.token}")
    private String token;

    @Value("${moodle.domainName}")
    private String domainName;

    private final RestTemplate restTemplate;
    public PersonalService() {
        this.restTemplate = new RestTemplate();
    }

    @Autowired
    private UsersService usersService;

    @Autowired
    private RecentlyAccessedCoursesRepository recentlyAccessedCoursesRepository;

    // Lấy danh sách các khóa học vừa truy cập của một người dùng
    public List<RecentlyAccessedCourseDTO> getRecentlyAccessedCourses(Integer userId) {
        List<RecentlyAccessedCoursesEntity> courses = recentlyAccessedCoursesRepository.findByUserIdOrderByAccessedAtDesc(userId);
        System.out.println("Recently accessed courses for user " + userId + ": " + courses);

        // Chuyển đổi các entities thành DTO và thêm thông tin chi tiết
        return courses.stream()
                .map(course -> {
                    RecentlyAccessedCourseDTO dto = course.toDTO();
                    // Bạn có thể bổ sung thêm các thông tin chi tiết vào DTO nếu cần
                    // Ví dụ: thiết lập tên khóa học, danh mục, giảng viên từ entity
                    dto.setCourseName(course.getCourseName());
                    dto.setCategoryName(course.getCategoryName());
                    dto.setInstructorName(course.getInstructorName());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
