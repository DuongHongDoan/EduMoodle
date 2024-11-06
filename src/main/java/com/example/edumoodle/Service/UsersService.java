package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.EnrolUserDTO;
import com.example.edumoodle.DTO.NguoiDungDTO;
import com.example.edumoodle.DTO.UsersDTO;
import com.example.edumoodle.DTO.UsersResponseDTO;
import com.example.edumoodle.Model.*;
import com.example.edumoodle.Repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UsersService {

    @Value("${moodle.token}")
    private String token;

    @Value("${moodle.domainName}")
    private String domainName;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private RolesRepository rolesRepository;
    @Autowired
    private CoursesRepository coursesRepository;
    @Autowired
    private CourseGroupsRepository courseGroupsRepository;
    @Autowired
    private CourseAssignmentRepository courseAssignmentRepository;

    @Autowired
    private UserInterface userInterface;

    private final RestTemplate restTemplate;
    public UsersService() {
        this.restTemplate = new RestTemplate();
    }

    private static final Logger logger = LoggerFactory.getLogger(UsersService.class);

    //    lấy người dùng theo username
    public UsersDTO getUserByUsername(String username) {
        String apiMoodleFunc = "core_user_get_users";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json"
                + "&criteria[0][key]=username"
                + "&criteria[0][value]=" + username;

        UsersResponseDTO response = restTemplate.getForObject(url, UsersResponseDTO.class);
        return (response != null && response.getUsers() != null && !response.getUsers().isEmpty())
                ? response.getUsers().get(0)
                : null;
    }

    // Lấy danh sách giảng viên và sinh viên của khóa học
    public List<UsersDTO> getEnrolledUsers(Integer courseId) {
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

    //Đăng ký người dùng vào khóa học
    public void enrolUser(EnrolUserDTO enrolUserDTO) {
        String apiMoodleFunc = "enrol_manual_enrol_users";
        StringBuilder url = new StringBuilder(domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json");

        for(int i=0; i<enrolUserDTO.getUserid().size(); i++) {
            Integer userid = enrolUserDTO.getUserid().get(i);
            url.append("&enrolments[").append(i).append("][roleid]=").append(enrolUserDTO.getRoleid())
                    .append("&enrolments[").append(i).append("][userid]=").append(userid)
                    .append("&enrolments[").append(i).append("][courseid]=").append(enrolUserDTO.getCourseid());
        }

        restTemplate.postForObject(url.toString(), null, String.class);
    }

    public List<EnrolUserDTO> parseFileEnrolUsers(MultipartFile file, String fileType) throws IOException {
        List<EnrolUserDTO> enrolUsers = new ArrayList<>();

        if ("basicEnrol".equalsIgnoreCase(fileType)) {
            enrolUsers = parseCSVFileEnrolUsers(file);
        } else if ("DHCTEnrol".equalsIgnoreCase(fileType)) {
            enrolUsers = parseExcelFileEnrolUsers(file);
        }
        return enrolUsers;
    }

    private String getStringCellValue(Cell cell, int rowIndex, int colIndex) {
        if (cell == null) {
            throw new IllegalArgumentException("Ô tại dòng " + rowIndex + ", cột " + colIndex + " là null.");
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue()).trim();  // Chuyển số thành String nếu cần
        } else {
            throw new IllegalArgumentException("Ô tại dòng " + rowIndex + ", cột " + colIndex + " không phải là kiểu STRING hoặc NUMERIC.");
        }
    }

    public List<EnrolUserDTO> parseExcelFileEnrolUsers(MultipartFile file) throws IOException {
        List<EnrolUserDTO> enrolUsers = new ArrayList<>();

        try(Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            String teacherCode = getStringCellValue(sheet.getRow(2).getCell(1), 2, 1); // Mã CB
            String courseCode = getStringCellValue(sheet.getRow(3).getCell(1), 3, 1); // Mã MH
            String courseGroup = getStringCellValue(sheet.getRow(3).getCell(3), 3, 3);// Mã nhóm MH
            //String academicYear = sheet.getRow(4).getCell(1).getStringCellValue().trim(); // Năm học
            //String semester = sheet.getRow(4).getCell(3).getStringCellValue().trim(); // Học kỳ

            List<Integer> userIdsGV = new ArrayList<>(List.of());
            List<Integer> userIdsHV = new ArrayList<>(List.of());
            //tìm gv
            String teacherCode00 = teacherCode.length() == 4 ? "00" + teacherCode : teacherCode;
            System.out.println("Lấy thông tin gv và course: " + teacherCode00 + ", " + courseCode + ", " + courseGroup);
            UsersDTO usersDTOGV = getUserByUsername(teacherCode00.trim());
            System.out.println("Xong lệnh UserDTO " + usersDTOGV.getFirstname());
            UsersEntity usersEntityGV = usersRepository.findByUsername(teacherCode00.trim());
            if(usersEntityGV != null) {
                userIdsGV.add(usersDTOGV.getId());
                CourseGroupsEntity courseGroupsEntity;
                if(courseGroup.length()==1) {
                    courseGroupsEntity = courseGroupsRepository.findByGroupName(courseCode + '0' + courseGroup);
                } else {
                    courseGroupsEntity = courseGroupsRepository.findByGroupName(courseCode + courseGroup);
                }
                if(courseGroupsEntity != null) {
                    Optional<CoursesEntity> coursesOpt = coursesRepository.findById(courseGroupsEntity.getCoursesEntity().getId_courses());
                    CoursesEntity coursesEntity = coursesOpt.get();
                    //xử lý role
                    String role = "editingteacher";
                    Optional<RolesEntity> rolesOpt = rolesRepository.findRoleByName(role);
                    if(rolesOpt.isPresent()) {
                        RolesEntity rolesEntity = rolesOpt.get();
                        //kiểm tra người dùng có đky course chưa
                        List<UserRoleEntity> userRoles = userRoleRepository.findByUsersEntityAndRolesEntity(usersEntityGV, rolesEntity);
                        int cnt = 0;
                        for(UserRoleEntity userRole : userRoles){
                            CourseAssignmentEntity courseAssignment = courseAssignmentRepository.findByCourseGroupsEntityAndUserRoleEntity(courseGroupsEntity, userRole);
                            if(courseAssignment == null) {
                                cnt++;
                            }else {
                                break;
                            }
                        }
                        if(cnt == userRoles.size()) {
                            EnrolUserDTO enrolUser = new EnrolUserDTO(userIdsGV, coursesEntity.getMoodleId(), rolesEntity.getMoodleId());
                            enrolUsers.add(enrolUser);
                        }else {
                            throw new IllegalArgumentException("Người dùng giảng viên'" + teacherCode00 + "' đã được đăng ký vào khóa học '" + courseCode + courseGroup + "'!");
                        }
                    }else {
                        throw new IllegalArgumentException("Cột role không hợp lệ, không phải '" + role);
                    }
                }else {
                    throw new IllegalArgumentException("Cột course_group_code không hợp lệ, vì khóa học'" + courseCode + courseGroup + "' chưa được tạo!");
                }
            }else {
                throw new IllegalArgumentException("Cột username không hợp lệ, vì người dùng'" + teacherCode00 + "' chưa được tạo!");
            }
            for(int rowIndex = 7; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if(row==null) continue;
                //lấy mã sv (username)
                Cell studentIdCell = row.getCell(1);
                if(studentIdCell == null || studentIdCell.getStringCellValue().isEmpty()) {
                    continue;
                }
                String studentId = getStringCellValue(studentIdCell, rowIndex, 1);
                System.out.println("Lấy thông tin sv: " + studentId);

                //tìm sv, tìm kiếm username có tồn tại không và thêm nó vào danh sách
                UsersDTO usersDTO = getUserByUsername(studentId);
                UsersEntity usersEntity = usersRepository.findByUsername(studentId);
                if(usersDTO != null) {
                    userIdsHV.add(usersDTO.getId());

                    //tìm nhóm học phần
                    CourseGroupsEntity courseGroupsEntity;
                    if(courseGroup.length()==1) {
                        courseGroupsEntity = courseGroupsRepository.findByGroupName(courseCode + '0' + courseGroup);
                    } else {
                        courseGroupsEntity = courseGroupsRepository.findByGroupName(courseCode + courseGroup);
                    }
                    if(courseGroupsEntity != null) {
                        Optional<CoursesEntity> coursesOpt = coursesRepository.findById(courseGroupsEntity.getCoursesEntity().getId_courses());
                        CoursesEntity coursesEntity = coursesOpt.get();
                        //xử lý role
                        String role = "student";
                        Optional<RolesEntity> rolesOpt = rolesRepository.findRoleByName(role);
                        if(rolesOpt.isPresent()) {
                            RolesEntity rolesEntity = rolesOpt.get();
                            //kiểm tra người dùng có đky course chưa
                            List<UserRoleEntity> userRoles = userRoleRepository.findByUsersEntityAndRolesEntity(usersEntity, rolesEntity);
                            int cnt = 0;
                            for(UserRoleEntity userRole : userRoles){
                                CourseAssignmentEntity courseAssignment = courseAssignmentRepository.findByCourseGroupsEntityAndUserRoleEntity(courseGroupsEntity, userRole);
                                if(courseAssignment == null) {
                                    cnt++;
                                }else {
                                    break;
                                }
                            }
                            if(cnt == userRoles.size()) {
                                EnrolUserDTO enrolUser = new EnrolUserDTO(userIdsHV, coursesEntity.getMoodleId(), rolesEntity.getMoodleId());
                                enrolUsers.add(enrolUser);
                            }else {
                                throw new IllegalArgumentException("Người dùng học viên'" + studentId + "' đã được đăng ký vào khóa học '" + courseCode + courseGroup + "'!");
                            }
                        }else {
                            throw new IllegalArgumentException("Cột role không hợp lệ, không phải '" + role);
                        }
                    }else {
                        throw new IllegalArgumentException("Cột course_group_code không hợp lệ, vì khóa học'" + courseCode + courseGroup + "' chưa được tạo!");
                    }
                }else {
                    throw new IllegalArgumentException("Cột username không hợp lệ, vì người dùng'" + studentId + "' chưa được tạo!");
                }
            }
        }
        return enrolUsers;
    }

    //đăng ký bằng cách upload file ds
    public List<EnrolUserDTO> parseCSVFileEnrolUsers(MultipartFile file) throws IOException {
        List<EnrolUserDTO> enrolUsers = new ArrayList<>();
        Set<String> validFields = Set.of("username", "course_group_code", "role");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine(); //đọc dòng đầu tiên của file
            if (headerLine == null) {
                throw new IllegalArgumentException("File CSV không có nội dung");
            }

            String[] headers = headerLine.split(",");
            for (String header : headers) {
                if (!validFields.contains(header.trim().toLowerCase())) {
                    throw new IllegalArgumentException("Trường " + header + " không hợp lệ!");
                }
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");

                Map<String, String> userMap = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    userMap.put(headers[i].trim().toLowerCase(), fields[i].trim());
                }

                if (userMap.get("username") == null || userMap.get("username").isEmpty()) {
                    throw new IllegalArgumentException("Trường 'username' không được để trống");
                }
                if (userMap.get("course_group_code") == null || userMap.get("course_group_code").isEmpty()) {
                    throw new IllegalArgumentException("Trường 'course_group_code' không được để trống");
                }
                if (userMap.get("role") == null || userMap.get("role").isEmpty()) {
                    throw new IllegalArgumentException("Trường 'role' không được để trống");
                }

                List<Integer> userIds = new ArrayList<>(List.of());
                UsersDTO usersDTO = getUserByUsername(userMap.getOrDefault("username", ""));
                UsersEntity usersEntity = usersRepository.findByUsername(userMap.getOrDefault("username", ""));
                if(usersDTO != null) {
                    userIds.add(usersDTO.getId());

                    String groupName = userMap.getOrDefault("course_group_code", "");
                    CourseGroupsEntity courseGroupsEntity = courseGroupsRepository.findByGroupName(groupName);
                    if(courseGroupsEntity != null) {
                        Optional<CoursesEntity> coursesOpt = coursesRepository.findById(courseGroupsEntity.getCoursesEntity().getId_courses());
                        CoursesEntity coursesEntity = coursesOpt.get();
                        if(Objects.equals(userMap.get("role"), "sv")) {
                            userMap.put("role", "student");
                        }
                        if(Objects.equals(userMap.get("role"), "gv")) {
                            userMap.put("role", "editingteacher");
                        }

                        Optional<RolesEntity> rolesOpt = rolesRepository.findRoleByName(userMap.get("role"));
                        if(rolesOpt.isPresent()) {
                            RolesEntity rolesEntity = rolesOpt.get();

                            //kiểm tra người dùng có đăng ký vào khóa học chưa?
                            List<UserRoleEntity> userRoles = userRoleRepository.findByUsersEntityAndRolesEntity(usersEntity, rolesEntity);
                            int cnt = 0;
                            for(UserRoleEntity userRole : userRoles){
                                CourseAssignmentEntity courseAssignment = courseAssignmentRepository.findByCourseGroupsEntityAndUserRoleEntity(courseGroupsEntity, userRole);
                                if(courseAssignment == null) {
                                    cnt++;
                                }else {
                                    break;
                                }
                            }
                            if(cnt == userRoles.size()) {
                                EnrolUserDTO enrolUser = new EnrolUserDTO(userIds, coursesEntity.getMoodleId(), rolesEntity.getMoodleId());
                                enrolUsers.add(enrolUser);
                            }else {
                                throw new IllegalArgumentException("Người dùng '" + userMap.get("username") + "' đã được đăng ký vào khóa học '" + userMap.get("course_group_code") + "'!");
                            }
                        }else {
                            throw new IllegalArgumentException("Cột role không hợp lệ, không phải '" + userMap.get("role") + "' mà nó phải là hoặc 'gv' hoặc 'sv'!");
                        }
                    }else {
                        throw new IllegalArgumentException("Cột course_group_code không hợp lệ, vì khóa học'" + userMap.get("course_group_code") + "' chưa được tạo!");
                    }
                }else {
                    throw new IllegalArgumentException("Cột username không hợp lệ, vì người dùng'" + userMap.get("username") + "' chưa được tạo!");
                }
            }
        }
        return enrolUsers;
    }

    //Hủy đăng ký khóa học cho người dùng
    public void unEnrolUser(Integer userid, Integer courseId) {
        String apiMoodleFunc = "enrol_manual_unenrol_users";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json"
                + "&enrolments[0][userid]=" + userid
                + "&enrolments[0][courseid]=" + courseId;

        restTemplate.getForEntity(url, String.class);
    }

    //hủy đăng ký khóa học csdl web
    public void unEnrolUserWeb(Integer userId, Integer courseId) {
        Optional<CoursesEntity> courseOpt = coursesRepository.findByMoodleId(courseId);
        if(courseOpt.isPresent()) {
            CourseGroupsEntity courseGroup = courseGroupsRepository.findByCoursesEntity(courseOpt.get());
            if(courseGroup != null) {
                Optional<UsersEntity> userOpt = usersRepository.findByMoodleId(userId);
                if(userOpt.isPresent()) {
                    List<UserRoleEntity> userRoles = userRoleRepository.findByUsersEntity(userOpt.get());
                    for(UserRoleEntity userRole : userRoles) {
                        CourseAssignmentEntity courseAssignment =
                                courseAssignmentRepository.findByCourseGroupsEntityAndUserRoleEntity(courseGroup, userRole);
                        if(courseAssignment != null) {
                            courseAssignmentRepository.deleteById(courseAssignment.getId_course_assign());
                            userRoleRepository.deleteById(userRole.getId());
                        }else {
                            System.out.println("No course assignment found for role: " + userRole.getId());
                        }
                    }
                } else {
                    System.out.println("User not found.");
                }
            } else {
                System.out.println("Course group not found.");
            }
        } else {
            System.out.println("Course not found.");
        }
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

    //lấy danh sách người dùng role=admin trong csdl web
    public List<UsersEntity> getUserByRoleAdmin(String roleName) {
        return usersRepository.findUsersByRoleName(roleName);
    }

    // Xóa vai trò ADMIN của người dùng
    @Transactional
    public void removeAdminRole(Integer userId) throws Exception {
        // Lấy người dùng dựa trên userId
        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new Exception("Người dùng không tồn tại"));

        // Tìm role ADMIN
        RolesEntity adminRole = rolesRepository.findRoleByName("ADMIN")
                .orElseThrow(() -> new Exception("Vai trò ADMIN không tồn tại"));

        // Kiểm tra xem người dùng có vai trò ADMIN không
        if (user.getUserRole().stream().anyMatch(ur -> ur.getRolesEntity().equals(adminRole))) {
            // Xóa vai trò admin khỏi userRole
            userRoleRepository.deleteByUsersEntityAndRolesEntity(user, adminRole);
        } else {
            throw new Exception("Người dùng không có vai trò ADMIN");
        }
    }

    //xử lý dữ liệu từ form ô tìm kiếm người dùng
    public List<UsersDTO> getSearchUser(String keyword) {
        List<UsersDTO> users = getAllUsers();

        return users.stream()
                .filter(user -> {
                    String firstname = user.getFirstname().toLowerCase();
                    String lastname = user.getLastname().toLowerCase();
                    String fullname = (firstname + " " + lastname).toLowerCase();
                    String keywordLower = keyword.toLowerCase();

                    // Kiểm tra nếu keyword có trong firstname, lastname, hoặc fullname
                    return firstname.contains(keywordLower) ||
                            lastname.contains(keywordLower) ||
                            fullname.contains(keywordLower);
                })
                .collect(Collectors.toList());
    }

    //    lưu tất cả người dùng được thêm vào csdl web
    public void saveUsers(List<UsersDTO> users) {
        // Lấy tất cả users hiện có từ cơ sở dữ liệu web
        List<UsersEntity> existingUsers = usersRepository.findAll();

        // Tạo một tập hợp các ID của users từ Moodle để kiểm tra
        Set<Integer> moodleIds = users.stream()
                .map(UsersDTO::getId)
                .collect(Collectors.toSet());

        // Cập nhật hoặc lưu các user từ Moodle
        for (UsersDTO dto : users) {
            Optional<UsersEntity> existingUser = existingUsers.stream()
                    .filter(c -> c.getMoodleId().equals(dto.getId()))
                    .findFirst();

            if (existingUser.isPresent()) {
                // Nếu user đã tồn tại, kiểm tra và cập nhật nếu cần thiết
                UsersEntity user = existingUser.get();
                boolean updated = false;

                if (!user.getUsername().equals(dto.getUsername())) {
                    user.setUsername(dto.getUsername());
                    updated = true;
                }
                if (!user.getFirstname().equals(dto.getFirstname())) {
                    user.setFirstname(dto.getFirstname());
                    updated = true;
                }
                if (!user.getLastname().equals(dto.getLastname())) {
                    user.setLastname(dto.getLastname());
                    updated = true;
                }
                if (!user.getEmail().equals(dto.getEmail())) {
                    user.setEmail(dto.getEmail());
                    updated = true;
                }
//                if(!user.getPassword().equals(dto.getPassword())) {
//                    user.setPassword(dto.getPassword());
//                    updated = true;
//                }

                // Lưu nếu có thay đổi
                if (updated) {
                    usersRepository.save(user);
                }
            } else {
                // Nếu danh mục không tồn tại, tạo mới
                UsersEntity newUser = new UsersEntity();
                newUser.setMoodleId(dto.getId());
                newUser.setUsername(dto.getUsername());
                newUser.setPassword(dto.getPassword());
                newUser.setFirstname(dto.getFirstname());
                newUser.setLastname(dto.getLastname());
                newUser.setEmail(dto.getEmail());
                usersRepository.save(newUser);
            }
        }

        // Xóa các danh mục không còn tồn tại trên Moodle
        for (UsersEntity existingUser : existingUsers) {
            if (!moodleIds.contains(existingUser.getMoodleId())) {
                usersRepository.delete(existingUser);
            }
        }
    }

    private static final Set<String> VALID_FIELDS = Set.of(
            "username", "password", "firstname", "lastname", "email"
    );
    public List<UsersDTO> parseCSVFileCreateUser(MultipartFile file) throws IOException {
        List<UsersDTO> users = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine(); //đọc dòng đầu tiên của file (đây là các trường dùng để tạo user)
            if (headerLine == null) {
                throw new IllegalArgumentException("File CSV không có nội dung");
            }

            String[] headers = headerLine.split(",");
            for (String header : headers) {
                if (!VALID_FIELDS.contains(header.trim().toLowerCase())) {
                    throw new IllegalArgumentException("Trường " + header + " không hợp lệ!");
                }
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");

                Map<String, String> userMap = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    userMap.put(headers[i].trim().toLowerCase(), fields[i].trim());
                }

                if (userMap.get("username") == null || userMap.get("username").isEmpty()) {
                    throw new IllegalArgumentException("Trường 'username' không được để trống");
                }
                if (userMap.get("firstname") == null || userMap.get("firstname").isEmpty()) {
                    throw new IllegalArgumentException("Trường 'firstname' không được để trống");
                }
                if (userMap.get("lastname") == null || userMap.get("lastname").isEmpty()) {
                    throw new IllegalArgumentException("Trường 'lastname' không được để trống");
                }
                if (userMap.get("email") == null || userMap.get("email").isEmpty()) {
                    throw new IllegalArgumentException("Trường 'email' không được để trống");
                }
                if (userMap.get("email") != null && !userMap.get("email").matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    throw new IllegalArgumentException("Định dạng email không hợp lệ: " + userMap.get("email"));
                }
                if (userMap.get("password") == null || userMap.get("password").isEmpty()) {
                    throw new IllegalArgumentException("Trường 'password' không được để trống");
                }

                UsersDTO user = new UsersDTO(
                        userMap.getOrDefault("username", ""),
                        userMap.getOrDefault("firstname", ""),
                        userMap.getOrDefault("lastname", ""),
                        userMap.getOrDefault("email", ""),
                        userMap.getOrDefault("password", "")
                );

                users.add(user);
            }
        }
        return users;
    }

    //thêm thành viên mới
    public String createNewUser(UsersDTO usersDTO) {
        String apiMoodleFunc = "core_user_create_users";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json"
                + "&users[0][username]=" + usersDTO.getUsername()
                + "&users[0][password]=" + usersDTO.getPassword()
                + "&users[0][firstname]=" + usersDTO.getFirstname()
                + "&users[0][lastname]=" + usersDTO.getLastname()
                + "&users[0][email]=" + usersDTO.getEmail();

        String response = restTemplate.postForObject(url, null, String.class);

        // Phân tích JSON để lấy user ID từ Moodle
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response);

            // Kiểm tra nếu phản hồi là mảng JSON
            if (root.isArray() && !root.isEmpty()) {
                JsonNode firstUser = root.get(0); // Lấy phần tử đầu tiên của mảng
                // Lấy user ID
                return firstUser.get("id").asText();
            }
            // Kiểm tra nếu phản hồi là object JSON (không phải mảng)
            else if (root.isObject()) {
                // Lấy user ID từ object
                return root.get("id").asText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //kiểm tra các input tạo người dùng
    public String validateCreateUser(UsersDTO userDto) {
        // Kiểm tra username đã tồn tại
        UsersEntity existingUser = userInterface.findByUsername(userDto.getUsername());
        if (existingUser != null) {
            return "Username đã tồn tại.";
        }

        // Kiểm tra mật khẩu rỗng
        if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
            return "Password không được để trống.";
        }

        // Kiểm tra mật khẩu có chứa ký tự đặc biệt
        if (!userDto.getPassword().matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            return "Password phải chứa ít nhất một ký tự đặc biệt.";
        }

        // Kiểm tra mật khẩu có chứa ít nhất một chữ cái viết hoa
        if (!userDto.getPassword().matches(".*[A-Z].*")) {
            return "Password phải chứa ít nhất một chữ cái viết hoa.";
        }

        // Kiểm tra mật khẩu có chứa ít nhất một chữ cái viết thường
        if (!userDto.getPassword().matches(".*[a-z].*")) {
            return "Password phải chứa ít nhất một chữ cái viết thường.";
        }

        return null; // Không có lỗi
    }
    //lấy người dùng trong csdl web bằng moodleId chuyển về dữ liệu DTO tương tác moodle
    public UsersDTO getUserByMoodleID(Integer moodleId) {
        Optional<UsersEntity> userOption = usersRepository.findByMoodleId(moodleId);
        if (userOption.isPresent()) {
            UsersEntity userEntity = userOption.get();
            // Chuyển đổi từ UsersEntity sang UsersDTO
            UsersDTO userDTO = new UsersDTO();
            userDTO.setId(userEntity.getMoodleId());
            userDTO.setUsername(userEntity.getUsername());
            userDTO.setFirstname(userEntity.getFirstname());
            userDTO.setLastname(userEntity.getLastname());
            userDTO.setEmail(userEntity.getEmail());
            return userDTO;
        }
        return null;
    }
    //lấy người dùng trong csdl web bằng moodleId
    public UsersEntity getUserByMoodleIDWeb(Integer moodleId) {
        Optional<UsersEntity> userOption = usersRepository.findByMoodleId(moodleId);
        if (userOption.isPresent()) {
            return userOption.get();
        }
        return null;
    }

    //    lấy người dùng theo id
    public UsersDTO getUserByID(Integer userid) {
        String apiMoodleFunc = "core_user_get_users";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json"
                + "&criteria[0][key]=id"
                + "&criteria[0][value]=" + userid;

        UsersResponseDTO response = restTemplate.getForObject(url, UsersResponseDTO.class);
        return (response != null && response.getUsers() != null && !response.getUsers().isEmpty())
                ? response.getUsers().get(0)
                : null;
    }
    public List<UsersDTO> getUsersByIds(List<Integer> userIds) {
        String url = "https://yourmoodlesite.com/webservice/rest/server.php"
                + "?wstoken=your_token"
                + "&wsfunction=core_user_get_users_by_field"
                + "&field=id"
                + "&moodlewsrestformat=json";

        for (int i = 0; i < userIds.size(); i++) {
            url += "&values[" + i + "]=" + userIds.get(i);
        }

        // Gọi API và ánh xạ kết quả trả về vào List<UsersDTO>
        UsersDTO[] usersArray = restTemplate.getForObject(url, UsersDTO[].class);
        assert usersArray != null;
        return Arrays.asList(usersArray);
    }

    //    cập nhật người dùng
    public boolean editUser(NguoiDungDTO usersDTO) {
        String apiMoodleFunc = "core_user_update_users";
        String url;
        if(!usersDTO.getPassword().isEmpty() && usersDTO.getPassword() != null) {
            url = domainName + "/webservice/rest/server.php"
                    + "?wstoken=" + token
                    + "&wsfunction=" + apiMoodleFunc
                    + "&moodlewsrestformat=json"
                    + "&users[0][id]=" + usersDTO.getId()
                    + "&users[0][username]=" + usersDTO.getUsername()
                    + "&users[0][password]=" + usersDTO.getPassword()
                    + "&users[0][firstname]=" + usersDTO.getFirstname()
                    + "&users[0][lastname]=" + usersDTO.getLastname()
                    + "&users[0][email]=" + usersDTO.getEmail();
        } else {
            url = domainName + "/webservice/rest/server.php"
                    + "?wstoken=" + token
                    + "&wsfunction=" + apiMoodleFunc
                    + "&moodlewsrestformat=json"
                    + "&users[0][id]=" + usersDTO.getId()
                    + "&users[0][username]=" + usersDTO.getUsername()
                    + "&users[0][firstname]=" + usersDTO.getFirstname()
                    + "&users[0][lastname]=" + usersDTO.getLastname()
                    + "&users[0][email]=" + usersDTO.getEmail();
        }
        // Log URL yêu cầu
//        System.out.println("Sending request to Moodle API: " + url);

        // Gửi yêu cầu cập nhật
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            // Log phản hồi
//            System.out.println("Response from Moodle API: " + response.getBody());
            if (response.getStatusCode() == HttpStatus.OK) {
                return true;  // Cập nhật thành công
            }
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
        return false;
    }

    //xóa người dùng
    public void deleteUser(Integer userId) {
        String apiMoodleFunc = "core_user_delete_users";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiMoodleFunc
                + "&moodlewsrestformat=json"
                + "&userids[0]=" + userId;

        restTemplate.getForEntity(url, String.class);
    }
}
