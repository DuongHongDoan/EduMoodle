package com.example.edumoodle.Controller.admin;

import com.example.edumoodle.DTO.*;
import com.example.edumoodle.Model.*;
import com.example.edumoodle.Repository.*;
import com.example.edumoodle.Service.CategoriesService;
import com.example.edumoodle.Service.CoursesService;
import com.example.edumoodle.Service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@Tag(name = "Courses Management", description = "APIs for managing Moodle courses")
public class CoursesController {

    @Autowired
    private CategoriesService categoriesService;

    @Autowired
    private CoursesService coursesService;
    @Autowired
    private UsersService usersService;

    @Autowired
    private CoursesRepository coursesRepository;
    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private RolesRepository rolesRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private SchoolYearSemesterRepository schoolYearSemesterRepository;
    @Autowired
    private CourseGroupsRepository courseGroupsRepository;
    @Autowired
    private CourseAssignmentRepository courseAssignmentRepository;

    @Operation(summary = "Get all categories for select input", description = "Fetch a list of all categories")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    //    url = /admin/courses
    @GetMapping("/courses")
    public String getCategoriesForSelect(@RequestParam(value = "page", defaultValue = "1") int page,
                                         @RequestParam(value = "size", defaultValue = "20") int size,Model model) {
        List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
        model.addAttribute("categoriesHierarchy", categoriesHierarchy);

        List<CoursesDTO> coursesList = coursesService.getAllCourses();
        model.addAttribute("coursesList", coursesList);

        Map<Integer, String> categoryMap = coursesService.getMapCategories();
        model.addAttribute("categoryMap", categoryMap);

        if (coursesList.size() > size) {
            // Phân trang
            int pageIndex = page - 1;
            int start = pageIndex * size;
            int end = Math.min(start + size, coursesList.size());
            List<CoursesDTO> pagedCourses = coursesList.subList(start, end);
            Page<CoursesDTO> coursePage = new PageImpl<>(pagedCourses, PageRequest.of(pageIndex, size), coursesList.size());
            model.addAttribute("coursePage", coursePage);
        }else {
            model.addAttribute("coursePage", null);
        }
        //đồng bộ dữ liệu course web-moodle
        coursesService.synchronizeCourses(coursesList);

        return "admin/ManageCourses";
    }

    @Operation(summary = "hiển thị courses từng category tương ứng", description = "Fetch a list of all courses")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    //   url = /admin/courses/category?categoryId=2
    @GetMapping("/courses/category")
    public String  getCoursesByParentCategory(@RequestParam(value = "categoryId", required = false) Integer categoryId,
                                              @RequestParam(value = "page", defaultValue = "1") int page,
                                              @RequestParam(value = "size", defaultValue = "20") int size, Model model) {
        List<CoursesDTO> coursesOfParent;

        if (categoryId != null) {
            coursesOfParent = coursesService.getCoursesByParentCategory(categoryId);  // Lấy khóa học theo category
        }else {
            coursesOfParent = coursesService.getAllCourses();  // Lấy tất cả khóa học nếu không chọn danh mục
        }

        model.addAttribute("coursesOfParent", coursesOfParent);
        model.addAttribute("categoryId", categoryId);

        Map<Integer, String> categoryMap = coursesService.getMapCategories();
        model.addAttribute("categoryMap", categoryMap);

        List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
        model.addAttribute("categoriesHierarchy", categoriesHierarchy);
        if(coursesOfParent.size() > size) {
            // Phân trang
            int pageIndex = page - 1;
            int start = pageIndex * size;
            int end = Math.min(start + size, coursesOfParent.size());
            List<CoursesDTO> pagedCourses = coursesOfParent.subList(start, end);
            Page<CoursesDTO> coursePage = new PageImpl<>(pagedCourses, PageRequest.of(pageIndex, size), coursesOfParent.size());
            model.addAttribute("coursePage", coursePage);
        } else {
            model.addAttribute("coursePage", null);
        }

        return "admin/ManageCourses";
    }

    @Operation(summary = "Display course detail", description = "click a course then display course detail")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved course detail")
    //    url = /admin/courses/view?courseId=
    @GetMapping("/courses/view")
    public String getCourseDetail(@RequestParam Integer courseId, Model model) {
        List<SectionsDTO> sections = coursesService.getCourseContent(courseId);
        model.addAttribute("sections", sections);

        CoursesDTO courseDetails = coursesService.getCourseDetail(courseId);
        model.addAttribute("courseDetails", courseDetails);
        List<UsersDTO> enrolledUsers = usersService.getEnrolledUsers(courseId);
        model.addAttribute("enrolledUsers", enrolledUsers);
        long studentCount = enrolledUsers.stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> "student".equals(role.getShortname())))
                .count();
        model.addAttribute("studentCount", studentCount);

        List<UsersDTO> usersList = usersService.getAllUsers();
        // Lọc ra những sinh viên chưa đăng ký khóa học
        List<UsersDTO> usersListNotEnrolled = usersList.stream()
                .filter(user -> user.getId() != 1 && user.getId() != 2) // Loại bỏ các user ID không liên quan
                .filter(user -> enrolledUsers.stream().noneMatch(enrolledUser -> enrolledUser.getId().equals(user.getId())))
                .toList();
        model.addAttribute("usersList", usersListNotEnrolled);
        model.addAttribute("course", courseId);

        return "admin/DetailCourse";
    }

    @Operation(summary = "Enrol users to course", description = "Enrol users to course")
    @ApiResponse(responseCode = "200", description = "Successfully enrolled user")
    //    url = /admin/courses/enrolUser
    @PostMapping("/courses/enrolUser")
    public String enrolUser(@RequestParam("userIds") List<Integer> userIds,
                            @RequestParam("roleId") Integer roleId,
                            @RequestParam("courseId") Integer courseId, RedirectAttributes redirectAttributes) {
        // Khởi tạo đối tượng DTO và gọi service để đăng ký người dun vào khóa học với role tương ứng
        EnrolUserDTO enrolUserDTO = new EnrolUserDTO();
        enrolUserDTO.setUserid(userIds);
        enrolUserDTO.setRoleid(roleId);
        enrolUserDTO.setCourseid(courseId);
        usersService.enrolUser(enrolUserDTO);

        //đăng ký vai trò student-teacher cho việc xác định role để đăng nhập
        for(Integer userId : userIds) {
            UsersEntity user = usersRepository.findByMoodleId(userId).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Người dùng không tồn tại.");
                return "redirect:/admin/users/manage-role";
            }

            RolesEntity roleName = rolesRepository.findByMoodleId(roleId).orElse(null);
            assert roleName != null;
            RolesEntity role = rolesRepository.findByName(roleName.getName());
            if (role == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vai trò không tồn tại.");
                return "redirect:/admin/users/manage-role";
            }

            // Tạo quan hệ giữa user và role
            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setUsersEntity(user);
            userRole.setRolesEntity(role);
            userRoleRepository.save(userRole);

            //đăng ký người dùng với khóa học tương ứng
            Optional<CoursesEntity> courseOpt = coursesRepository.findByMoodleId(courseId);
            if(courseOpt.isPresent()) {
                CoursesEntity course = courseOpt.get();
                CourseGroupsEntity courseGroup = courseGroupsRepository.findByCoursesEntity(course);

                CourseAssignmentEntity courseAssignment = new CourseAssignmentEntity();
                courseAssignment.setUserRoleEntity(userRole);
                courseAssignment.setCourseGroupsEntity(courseGroup);
                courseAssignmentRepository.save(courseAssignment);
            }
        }

        return "redirect:/admin/courses/view?courseId=" + enrolUserDTO.getCourseid();
    }

    @Operation(summary = "Unenrol user from course", description = "Unenrol user from course")
    @ApiResponse(responseCode = "200", description = "Successfully unenrolled user")
    //    url = /admin/courses/unenrol
    @GetMapping("/courses/unenrol")
    public String unEnrolUser(@RequestParam("userid") Integer userid,
                             @RequestParam("courseid") Integer courseid,
                             RedirectAttributes redirectAttributes) {
        try {
            usersService.unEnrolUser(userid, courseid);

            usersService.unEnrolUserWeb(userid, courseid);

            redirectAttributes.addFlashAttribute("successMessage", "Người dùng đã bị xóa khỏi khóa học.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa người dùng khỏi khóa học.");
        }
        return "redirect:/admin/courses/view?courseId=" + courseid;
    }

    @Operation(summary = "Display search course", description = "enter keyword in search input to search course")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved course list for search")
    //    url = /admin/courses/search
    @GetMapping("/courses/search")
    public String searchCourses(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<CoursesDTO> courses;

        if (keyword != null && !keyword.isEmpty()) {
            courses = coursesService.getSearchCourses(keyword);  // Tìm khóa học từ API Moodle
        } else {
            courses = coursesService.getAllCourses();  // Lấy tất cả khóa học nếu không có từ khóa
        }

        model.addAttribute("coursesList", courses);
        model.addAttribute("keyword", keyword);

        Map<Integer, String> categoryMap = coursesService.getMapCategories();
        model.addAttribute("categoryMap", categoryMap);

        List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
        model.addAttribute("categoriesHierarchy", categoriesHierarchy);

        return "admin/ManageCourses";
    }

    //url = /admin/courses/create-course
    @GetMapping("/courses/create-course")
    public String showFormCreateCourse(Model model) {
        List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
        model.addAttribute("categoriesHierarchy", categoriesHierarchy);

        CoursesDTO coursesDTO = new CoursesDTO();
        model.addAttribute("coursesDTO", coursesDTO);

        List<SchoolYearsEntity> schoolYearsEntities = coursesService.getAllSchoolYear();
        model.addAttribute("schoolYears", schoolYearsEntities);

        List<SemestersEntity> semestersEntities = coursesService.getAllSemester();
        model.addAttribute("semesters", semestersEntities);

        return "admin/CreateCourse";
    }

    @Operation(summary = "Create course", description = "create course")
    @ApiResponse(responseCode = "200", description = "Successfully created course")
    //    url = /admin/courses/create-course
    @PostMapping("/courses/create-course")
    public String createCourse(@Valid @ModelAttribute CoursesDTO coursesDTO,
                               @RequestParam("schoolYearName") Integer schoolYearName,
                               @RequestParam("semesterName") Integer semesterName,
                               @RequestParam("courseCode") String courseCode,
                               @RequestParam("groupName") String groupName,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes, Model model) {
        if(bindingResult.hasErrors()) {
            List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
            model.addAttribute("categoriesHierarchy", categoriesHierarchy);
            model.addAttribute("coursesDTO", coursesDTO);
            return "admin/CreateCourse";
        }

        // Tạo khóa học trên Moodle
        String moodleResponse = coursesService.createCourse(coursesDTO);

        if (moodleResponse != null && moodleResponse.contains("id")) {
            Integer moodleCourseId = coursesService.extractMoodleCourseId(moodleResponse);
            CategoriesEntity category = categoriesRepository.findByMoodleId(coursesDTO.getCategoryid());
            if (category == null) {
                // Xử lý lỗi nếu category không tồn tại
                model.addAttribute("error", "Category không tồn tại.");
                return "admin/CreateCourse";
            }

            //lưu in4 của khóa học vào các bảng trong csdl web
            CoursesEntity newCourse = new CoursesEntity();
            newCourse.setMoodleId(moodleCourseId);
            newCourse.setFullname(coursesDTO.getFullname());
            newCourse.setShortname(coursesDTO.getShortname());
            newCourse.setSummary(coursesDTO.getSummary());
            newCourse.setCategoriesEntity(category);
            coursesRepository.save(newCourse);

            SchoolYearSemesterEntity getSchoolYearSemesterEntity = coursesService.getOrCreateSchoolYearSemester(schoolYearName, semesterName);
            if (getSchoolYearSemesterEntity == null) {
                model.addAttribute("error", "Năm học hoặc học kỳ không tồn tại.");
                return "admin/CreateCourse";
            }
            CourseGroupsEntity courseGroupsEntity = new CourseGroupsEntity();
            courseGroupsEntity.setCourseCode(courseCode);
            courseGroupsEntity.setGroupName(groupName);
            courseGroupsEntity.setCoursesEntity(newCourse);
            courseGroupsEntity.setSchoolYearSemesterEntity(getSchoolYearSemesterEntity);
            courseGroupsRepository.save(courseGroupsEntity);

            redirectAttributes.addFlashAttribute("successMessage", "Tạo khóa học thành công!");
            return "redirect:/admin/courses";
        } else {
            // Nếu tạo khóa học thất bại, trả về thông báo lỗi
            model.addAttribute("errorMessage", "Failed to create the course on Moodle. Please try again.");
            List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
            model.addAttribute("categoriesHierarchy", categoriesHierarchy);
            model.addAttribute("coursesDTO", coursesDTO);
            return "admin/CreateCourse";
        }
    }

    //url = /admin/courses/create-course-list
    @GetMapping("/courses/create-course-list")
    public String showFormCreateCourseList(Model model) {
        List<SchoolYearsEntity> schoolYearsEntities = coursesService.getAllSchoolYear();
        model.addAttribute("schoolYears", schoolYearsEntities);

        List<SemestersEntity> semestersEntities = coursesService.getAllSemester();
        model.addAttribute("semesters", semestersEntities);

        return "admin/UploadCourseList";
    }

    @Operation(summary = "Upload course list", description = "Upload course list to create auto course")
    @ApiResponse(responseCode = "200", description = "Successfully create course list")
    //    url = /admin/courses/create-course-list
    @PostMapping("/courses/create-course-list")
    public String createCourseList(@RequestParam("file")MultipartFile file,
                                   @RequestParam("type") String type,
                                   @RequestParam("schoolYearName") Integer schoolYearName,
                                   @RequestParam("semesterName") Integer semesterName, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("errorMessage", "File không được để trống");
            return "admin/UploadCourseList";
        }

        String fileType = file.getContentType();
        if (!"text/csv".equals(fileType)) {
            model.addAttribute("errorMessage", "Chỉ chấp nhận file định dạng CSV");
            return "admin/UploadCourseList";
        }

        try {
            // Đọc và xử lý file CSV
            List<Map<String, String>> courses = coursesService.parseCSVFileCreateCourse(file, type);

            for (Map<String, String> course : courses) {
                String fullname = course.get("fullname");
                String shortname = course.get("shortname");
                String courseCode = course.get("courseCode");
                String courseGroupCode = course.get("courseGroupCode");
                Integer categoryId = Integer.valueOf(course.get("category"));
                String description = course.get("description");

                CoursesDTO courseDTO = new CoursesDTO(categoryId, fullname, shortname, description);
                String moodleResponse = coursesService.createCourse(courseDTO);

                if (moodleResponse != null && moodleResponse.contains("id")) {
                    Integer moodleCourseId = coursesService.extractMoodleCourseId(moodleResponse);
                    CategoriesEntity category = categoriesRepository.findByMoodleId(categoryId);
                    if (category == null) {
                        // Xử lý lỗi nếu category không tồn tại
                        model.addAttribute("error", "Category không tồn tại.");
                        return "admin/UploadCourseList";
                    }

                    //lưu in4 của khóa học vào các bảng trong csdl web
                    CoursesEntity newCourse = new CoursesEntity();
                    newCourse.setMoodleId(moodleCourseId);
                    newCourse.setFullname(fullname);
                    newCourse.setShortname(shortname);
                    newCourse.setSummary(description);
                    newCourse.setCategoriesEntity(category);
                    coursesRepository.save(newCourse);

                    SchoolYearSemesterEntity getSchoolYearSemesterEntity = coursesService.getOrCreateSchoolYearSemester(schoolYearName, semesterName);
                    if (getSchoolYearSemesterEntity == null) {
                        model.addAttribute("error", "Năm học hoặc học kỳ không tồn tại.");
                        return "admin/UploadCourseList";
                    }
                    CourseGroupsEntity courseGroupsEntity = new CourseGroupsEntity();
                    courseGroupsEntity.setCourseCode(courseCode);
                    courseGroupsEntity.setGroupName(courseGroupCode);
                    courseGroupsEntity.setCoursesEntity(newCourse);
                    courseGroupsEntity.setSchoolYearSemesterEntity(getSchoolYearSemesterEntity);
                    courseGroupsRepository.save(courseGroupsEntity);
                } else {
                    // Nếu tạo khóa học thất bại, trả về thông báo lỗi
                    model.addAttribute("errorMessage", "Lỗi tạo khóa học trên moodle. Vui lòng thử lại!");

                    List<SchoolYearsEntity> schoolYearsEntities = coursesService.getAllSchoolYear();
                    model.addAttribute("schoolYears", schoolYearsEntities);

                    List<SemestersEntity> semestersEntities = coursesService.getAllSemester();
                    model.addAttribute("semesters", semestersEntities);

                    return "admin/UploadCourseList";
                }
            }
            model.addAttribute("successMessage", "Tạo danh sách khóa học thành công!");
            return "admin/UploadCourseList";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());

            List<SchoolYearsEntity> schoolYearsEntities = coursesService.getAllSchoolYear();
            model.addAttribute("schoolYears", schoolYearsEntities);

            List<SemestersEntity> semestersEntities = coursesService.getAllSemester();
            model.addAttribute("semesters", semestersEntities);

            return "admin/UploadCourseList";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());

            List<SchoolYearsEntity> schoolYearsEntities = coursesService.getAllSchoolYear();
            model.addAttribute("schoolYears", schoolYearsEntities);

            List<SemestersEntity> semestersEntities = coursesService.getAllSemester();
            model.addAttribute("semesters", semestersEntities);

            return "admin/UploadCourseList";
        }
    }

    //    url = /admin/courses/edit-course?courseId= --> trả về view form sửa khóa học tương ứng
    @GetMapping("/courses/edit-course")
    public String showFormEditCourse(@RequestParam("courseId") Integer courseId, RedirectAttributes redirectAttributes, Model model) {
        Optional<CoursesEntity> courseOpt = coursesRepository.findByMoodleId(courseId);
        if (courseOpt.isPresent()) {
            CoursesEntity course = courseOpt.get();
            CoursesDTO coursesDto = new CoursesDTO();
            coursesDto.setId(course.getMoodleId());
            coursesDto.setFullname(course.getFullname());
            coursesDto.setShortname(course.getShortname());
            coursesDto.setCategoryid(course.getCategoriesEntity().getMoodleId());
            coursesDto.setSummary(course.getSummary());
            model.addAttribute("coursesDto", coursesDto);

            List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
            model.addAttribute("categoriesHierarchy", categoriesHierarchy);
            model.addAttribute("courseIdWeb", course.getId_courses());

            List<SchoolYearsEntity> schoolYearsEntities = coursesService.getAllSchoolYear();
            model.addAttribute("schoolYears", schoolYearsEntities);

            List<SemestersEntity> semestersEntities = coursesService.getAllSemester();
            model.addAttribute("semesters", semestersEntities);

            CourseGroupsEntity courseGroup = coursesService.findByCoursesId(course);
            if(courseGroup != null) {
                System.out.println("tìm nhóm HP đc: " + courseGroup.getCourseCode());
                model.addAttribute("courseCode", courseGroup.getCourseCode());
                model.addAttribute("groupName", courseGroup.getGroupName());

                SchoolYearSemesterEntity schoolYearSemester = coursesService.findByIdSchoolYearSemester(courseGroup.getSchoolYearSemesterEntity().getId_schoolYear_semester());
                System.out.println("tìm NH_HK: " + schoolYearSemester.getId_schoolYear_semester());
                model.addAttribute("schoolYearId", schoolYearSemester.getSchoolYearsEntity().getId_school_year());
                model.addAttribute("semesterId", schoolYearSemester.getSemestersEntity().getId_semester());
            }
        } else {
            model.addAttribute("errorMessage", "Course not found.");
        }
        return "admin/EditCourse";
    }

    @Operation(summary = "Edit course", description = "edit course")
    @ApiResponse(responseCode = "200", description = "Successfully edited course")
    //    url = /admin/courses/edit-course
    @PostMapping("/courses/edit-course")
    public String editCourse(@Valid @ModelAttribute CoursesDTO coursesDto, @RequestParam("courseIdWeb") Integer courseIdWeb,
                             @RequestParam("schoolYearName") Integer schoolYearName,
                             @RequestParam("semesterName") Integer semesterName,
                             @RequestParam("courseCode") String courseCode,
                             @RequestParam("groupName") String groupName,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
            model.addAttribute("categoriesHierarchy", categoriesHierarchy);
            model.addAttribute("coursesDto", coursesDto);
            model.addAttribute("courseIdWeb", courseIdWeb);
            return "admin/EditCourse";
        }

        Optional<CoursesEntity> courseOpt = coursesRepository.findByMoodleId(coursesDto.getId());
        CategoriesEntity category = categoriesRepository.findByMoodleId(coursesDto.getCategoryid());
        if (courseOpt.isPresent()) {
            CoursesEntity course = courseOpt.get();
            course.setMoodleId(coursesDto.getId());
            course.setFullname(coursesDto.getFullname());
            course.setShortname(coursesDto.getShortname());
            course.setCategoriesEntity(category); // Sử dụng Integer trực tiếp
            course.setSummary(coursesDto.getSummary());
            coursesRepository.save(course);

            CourseGroupsEntity courseGroup = coursesService.findByCoursesId(course);
            SchoolYearSemesterEntity getSchoolYearSemesterEntity = coursesService.getOrCreateSchoolYearSemester(schoolYearName, semesterName);
            if(courseGroup != null) { //tìm thấy course đã có nhóm học phan thì cập nhật lại nhóm học phần đó thoi
                courseGroup.setCourseCode(courseCode);
                courseGroup.setGroupName(groupName);
                courseGroup.setCoursesEntity(course);
                courseGroup.setSchoolYearSemesterEntity(getSchoolYearSemesterEntity);
                courseGroupsRepository.save(courseGroup);
            }
            else {//sửa các khóa học đc tạo trên moodle đồng bộ về web
                CourseGroupsEntity courseGroupsEntity = new CourseGroupsEntity();
                courseGroupsEntity.setCourseCode(courseCode);
                courseGroupsEntity.setGroupName(groupName);
                courseGroupsEntity.setCoursesEntity(course);
                courseGroupsEntity.setSchoolYearSemesterEntity(getSchoolYearSemesterEntity);
                courseGroupsRepository.save(courseGroupsEntity);
            }

            boolean moodleUpdated = coursesService.updateMoodleCourse(coursesDto);
            if (moodleUpdated) {
                redirectAttributes.addFlashAttribute("successMessage", "Sửa khóa học thành công!");
                return "redirect:/admin/courses";
            } else {
                model.addAttribute("errorMessage", "Cập nhật không thành công. Vui lòng thử lại!");
                List<CategoryHierarchyDTO> categoriesHierarchy = categoriesService.getParentChildCategories();
                model.addAttribute("categoriesHierarchy", categoriesHierarchy);
                model.addAttribute("coursesDto", coursesDto);
                model.addAttribute("courseIdWeb", courseIdWeb);
                return "admin/EditCourse";
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Course not found.");
            return "redirect:/admin/courses";
        }
    }

    @Operation(summary = "Delete course", description = "delete course")
    @ApiResponse(responseCode = "200", description = "Successfully deleted course")
    //    url = /admin/courses/delete
    @GetMapping("/courses/delete")
    public String deleteMoodleCourse(@RequestParam("courseId") Integer courseId, RedirectAttributes redirectAttributes) {
        boolean moodleSuccess = coursesService.deleteCourseFromMoodle(courseId);
        if (moodleSuccess) {
            boolean dbSuccess = coursesService.deleteCourseFromDatabase(courseId);
            if (dbSuccess) {
                redirectAttributes.addFlashAttribute("message", "Course deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to delete course from the database.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to delete course from Moodle.");
        }
        return "redirect:/admin/courses";
    }

    @Operation(summary = "Create Topic", description = "Create topic in course")
    @ApiResponse(responseCode = "200", description = "Successfully created topic")
    //    url = /admin/courses/view/create-topic
    @PostMapping("/courses/view/create-topic")
    public String createTopic(@RequestParam Integer courseId,
                              @RequestParam String name) {
        coursesService.createTopicInCourse(courseId, name);
        return "redirect:/admin/courses/view?courseId=" + courseId;
    }

    @Operation(summary = "Update Topic", description = "Update topic in course")
    @ApiResponse(responseCode = "200", description = "Successfully updated topic")
    //    url = /admin/courses/view/update-topic
    @PostMapping("/courses/view/update-topic")
    public String updateTopic(@RequestParam Integer sectionId, @RequestParam Integer courseId, @RequestParam String topicName) {
        coursesService.updateTopicInCourse(sectionId, courseId, topicName);
        return "redirect:/admin/courses/view?courseId=" + courseId;
    }

    @Operation(summary = "Delete Topic", description = "Delete topic in course")
    @ApiResponse(responseCode = "200", description = "Successfully deleted topic")
    //    url = /admin/courses/view/delete-topic
    @GetMapping("/courses/view/delete-topic")
    public String deleteTopic(@RequestParam Integer sectionId, @RequestParam Integer courseId) {
        coursesService.deleteTopicInCourse(sectionId, courseId);
        return "redirect:/admin/courses/view?courseId=" + courseId;
    }
}
