package com.example.edumoodle.Service;

import com.example.edumoodle.Configuration.HashPassword;
import com.example.edumoodle.DTO.EnrolUserDTO;
import com.example.edumoodle.DTO.NguoiDungDTO;
import com.example.edumoodle.DTO.UsersDTO;
import com.example.edumoodle.DTO.UsersResponseDTO;
import com.example.edumoodle.Model.UsersEntity;
import com.example.edumoodle.Repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UsersService {

    @Value("${moodle.token}")
    private String token;

    @Value("${moodle.domainName}")
    private String domainName;

    @Autowired
    private UsersRepository usersRepository;

    private final RestTemplate restTemplate;
    public UsersService() {
        this.restTemplate = new RestTemplate();
    }

    private static final Logger logger = LoggerFactory.getLogger(UsersService.class);

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
                if(!user.getPassword().equals(dto.getPassword())) {
                    user.setPassword(dto.getPassword());
                    updated = true;
                }

                // Lưu nếu có thay đổi
                if (updated) {
                    usersRepository.save(user);
                }
            } else {
                // Nếu danh mục không tồn tại, tạo mới
                UsersEntity newUser = new UsersEntity();
                newUser.setMoodleId(dto.getId());
                newUser.setAuth("manual");
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

//thêm thành viên mới
    public void createNewUser(UsersDTO usersDTO) {
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

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            // Xử lý phản hồi từ Moodle
            System.out.println("Response create user: " + response.getBody());
        } catch (Exception e) {
            // Xử lý ngoại lệ
            e.printStackTrace();
        }
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

    //    cập nhật người dùng
    public boolean editUser(NguoiDungDTO usersDTO) {
        String apiMoodleFunc = "core_user_update_users";
        String url;
        //băm mật khẩu
        if(!usersDTO.getPassword().isEmpty() && usersDTO.getPassword() != null) {
            String hashedPassword = HashPassword.hashPassword(usersDTO.getPassword());
            url = domainName + "/webservice/rest/server.php"
                    + "?wstoken=" + token
                    + "&wsfunction=" + apiMoodleFunc
                    + "&moodlewsrestformat=json"
                    + "&users[0][id]=" + usersDTO.getId()
                    + "&users[0][username]=" + usersDTO.getUsername()
                    + "&users[0][auth]=manual"
                    + "&users[0][password]=" + hashedPassword
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
                    + "&users[0][auth]=manual"
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
