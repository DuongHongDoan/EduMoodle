package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.QuizAttemptListDTO;
import com.example.edumoodle.DTO.QuizzesDTO;
import com.example.edumoodle.DTO.UsersDTO;
import com.example.edumoodle.Model.CourseGroupsEntity;
import com.example.edumoodle.Model.CoursesEntity;
import com.example.edumoodle.Model.SchoolYearSemesterEntity;
import com.example.edumoodle.Repository.CourseGroupsRepository;
import com.example.edumoodle.Repository.CoursesRepository;
import com.example.edumoodle.Repository.SchoolYearSemesterRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QuizService {

    @Autowired
    private UsersService usersService;

    @Autowired
    private CoursesRepository coursesRepository;
    @Autowired
    private CourseGroupsRepository courseGroupsRepository;
    @Autowired
    private SchoolYearSemesterRepository schoolYearSemesterRepository;

    @Value("${moodle.token}")
    private String token;

    @Value("${moodle.domainName}")
    private String domainName;

    private final RestTemplate restTemplate;
    public QuizService() {
        this.restTemplate = new RestTemplate();
    }

    //lấy info của 1 quiz
    public QuizzesDTO.QuizzesListDTO getQuizInCourse(Integer quizId, Integer courseId) {
        String apiQuizFunc = "mod_quiz_get_quizzes_by_courses";
        String url = domainName + "/webservice/rest/server.php"
                + "?wstoken=" + token
                + "&wsfunction=" + apiQuizFunc
                + "&moodlewsrestformat=json"
                + "&courseids[0]=" + courseId;

        QuizzesDTO.QuizzesListDTO quiz = null;
        QuizzesDTO response = restTemplate.getForObject(url, QuizzesDTO.class);
        if(response != null && response.getQuizzes() != null) {
            for(QuizzesDTO.QuizzesListDTO res : response.getQuizzes()) {
                if(res.getId().equals(quizId)) {
                    quiz = res;
                }
            }
        }
        return quiz;
    }

    //lấy thông tin chi tiết của một bài thi
    public List<QuizAttemptListDTO.AttemptDTO> getAllAttemptStudents(Integer quizId, Integer courseId) {
        String apiMoodleFunc = "mod_quiz_get_user_attempts";
        String apiReviewFunc = "mod_quiz_get_attempt_review";

        List<QuizAttemptListDTO.AttemptDTO> allAttempts = new ArrayList<>();
        //lấy danh sách id của sv đã đăng ký vào course
        List<UsersDTO> usersEnrolled = usersService.getEnrolledUsers(courseId);
        List<UsersDTO> studentsEnrolled = usersEnrolled.stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.getRoleid() == 5))
                .toList();
        List<Integer> userIds = new ArrayList<>();
        for (UsersDTO user : studentsEnrolled) {
            userIds.add(user.getId());
        }
        //lặp qua ds sv đky trên --> lấy attempt của sv ấy
        for(Integer userId : userIds) {
            String url = domainName + "/webservice/rest/server.php"
                    + "?wstoken=" + token
                    + "&wsfunction=" + apiMoodleFunc
                    + "&moodlewsrestformat=json"
                    + "&quizid=" + quizId
                    + "&userid=" + userId;
            QuizAttemptListDTO response = restTemplate.getForObject(url, QuizAttemptListDTO.class);
            if(response != null && response.getAttempts() != null) {
                for(QuizAttemptListDTO.AttemptDTO attempt : response.getAttempts()) {
                    attempt.setUsersDTO(usersService.getUserByID(userId));
                    //tính thời gian làm bài
                    QuizAttemptListDTO.AttemptDTO durationAttempt = new QuizAttemptListDTO.AttemptDTO(attempt.getTimestart(), attempt.getTimefinish());
                    attempt.setDuration(durationAttempt.getDurationFormat());
                    // Lấy điểm chi tiết từ API mod_quiz_get_attempt_review
                    String reviewUrl = domainName + "/webservice/rest/server.php"
                            + "?wstoken=" + token
                            + "&wsfunction=" + apiReviewFunc
                            + "&moodlewsrestformat=json"
                            + "&attemptid=" + attempt.getId();
                    QuizAttemptListDTO reviewResponse = restTemplate.getForObject(reviewUrl, QuizAttemptListDTO.class);

                    if (reviewResponse != null && reviewResponse.getGrade() != null) {
                        attempt.setGrade(reviewResponse.getGrade());
                    }
                }
                allAttempts.addAll(response.getAttempts());
            }
        }
        return allAttempts;
    }

    //export thông tin quiz ra file excel
    public void exportAttemptToExcel(QuizzesDTO.QuizzesListDTO quizDTO, List<QuizAttemptListDTO.AttemptDTO> attemptDTO, List<UsersDTO>enrolledStudents, String filePath, Integer courseId) throws IOException {
        //lấy ra gv đã đăng ký vào course
        List<UsersDTO> usersEnrolled = usersService.getEnrolledUsers(courseId);
        UsersDTO selectedTeacher = usersEnrolled.stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.getRoleid() == 3))
                .filter(user -> !"admin".equalsIgnoreCase(user.getUsername()))
                .findFirst()
                .orElse(null);
        assert selectedTeacher != null;
        //lấy mã nhóm, mã học phần
        Optional<CoursesEntity> coursesEntity = coursesRepository.findByMoodleId(courseId);
        CourseGroupsEntity courseGroups = courseGroupsRepository.findByCoursesEntity(coursesEntity.get());
        //lấy năm học - học kỳ
        SchoolYearSemesterEntity schoolYearSemester = schoolYearSemesterRepository.findByCourseGroups(courseGroups);

        //tạo tài liệu excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(quizDTO.getName());

        //tạo font và style
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 10);
        titleFont.setFontName("Times New Roman");

        Font tableData = workbook.createFont();
        tableData.setFontHeightInPoints((short) 10);
        tableData.setFontName("Times New Roman");

        // Tạo CellStyle căn giữa và áp dụng font Times New Roman
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(titleFont);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(titleFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle tableDataStyle = workbook.createCellStyle();
        tableDataStyle.setFont(tableData);
        tableDataStyle.setAlignment(HorizontalAlignment.CENTER);
        tableDataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Tạo CellStyle cho định dạng số
        CellStyle numberStyle = workbook.createCellStyle();
        numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.0"));
        numberStyle.setFont(tableData);

        //tiêu đề: tên quiz
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(quizDTO.getName().toUpperCase());
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 3));

        //tạo các dòng thông tin khóa học
        Row row1 = sheet.createRow(2);
        row1.createCell(0).setCellValue("Mã CB");
        row1.createCell(1).setCellValue(selectedTeacher.getUsername());
        row1.createCell(2).setCellValue("Họ và Tên");
        row1.createCell(3).setCellValue(selectedTeacher.getFirstname());
        applyCellStyleToRow(row1, cellStyle);

        Row row2 = sheet.createRow(3);
        row2.createCell(0).setCellValue("Mã MH");
        row2.createCell(1).setCellValue(courseGroups.getCourseCode());
        row2.createCell(2).setCellValue("Mã NH");
        row2.createCell(3).setCellValue(courseGroups.getGroupName());
        applyCellStyleToRow(row2, cellStyle);

        Row row3 = sheet.createRow(4);
        row3.createCell(0).setCellValue("Năm học");
        row3.createCell(1).setCellValue(schoolYearSemester.getSchoolYearsEntity().getSchoolYearName());
        row3.createCell(2).setCellValue("Học kỳ");
        row3.createCell(3).setCellValue(schoolYearSemester.getSemestersEntity().getSemesterName());
        applyCellStyleToRow(row3, cellStyle);

        //tạo header bảng danh sách sinh viên
        Row rowTableHeader = sheet.createRow(6);
        String[] headers = {"STT", "Mã sinh viên", "Họ và Tên", "Điểm/"+quizDTO.getFormattedGrade()};
        for(int i=0; i< headers.length; i++) {
            Cell cell = rowTableHeader.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            // Tự động điều chỉnh kích thước cột
            sheet.autoSizeColumn(i);
        }

        //Ghi dữ liệu vào bảng
        int rowTableData = 7;
        int stt = 1;
        for(UsersDTO student : enrolledStudents) {
            Row row = sheet.createRow(rowTableData++);
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(stt++);
            cell0.setCellStyle(tableDataStyle);

            Cell cell1 = row.createCell(1);
            cell1.setCellValue(student.getLastname());
            cell1.setCellStyle(tableDataStyle);

            Cell cell2 = row.createCell(2);
            cell2.setCellValue(student.getFirstname());
            cell2.setCellStyle(tableDataStyle);

            Cell gradeCell = row.createCell(3);
            Optional<QuizAttemptListDTO.AttemptDTO> studentAttempt = attemptDTO.stream()
                    .filter(attempt -> attempt.getUserid().equals(student.getId()))
                    .findFirst();
            if (studentAttempt.isPresent()) {
                if (studentAttempt.get().getGrade() != null) {
                    gradeCell.setCellValue(studentAttempt.get().getGrade().doubleValue());
                }
            } else {
                // Nếu sinh viên không có bài thi
                gradeCell.setCellValue(-5.00);
            }
            gradeCell.setCellStyle(numberStyle);
        }

        // Auto-size tất cả các cột sau khi ghi dữ liệu
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Ghi workbook ra file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }

        workbook.close();
    }
    // áp dụng style cho cùng 1 row
    private void applyCellStyleToRow(Row row, CellStyle cellStyle) {
        for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
            row.getCell(i).setCellStyle(cellStyle);
        }
    }
}
