package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.*;
import com.example.edumoodle.Model.CourseGroupsEntity;
import com.example.edumoodle.Model.CoursesEntity;
import com.example.edumoodle.Model.SchoolYearSemesterEntity;
import com.example.edumoodle.Repository.CourseGroupsRepository;
import com.example.edumoodle.Repository.CoursesRepository;
import com.example.edumoodle.Repository.SchoolYearSemesterRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    //ô tìm kiếm bài thi
    public List<QuizAttemptListDTO.AttemptDTO> getSearchAttemptByStudentName(String keyword, Integer courseId, Integer quizId) {
        List<QuizAttemptListDTO.AttemptDTO> attempts = getAllAttemptStudents(quizId, courseId);
        if (keyword != null && !keyword.isEmpty()) {
            String lowerCaseQuery = keyword.toLowerCase();
            attempts = attempts.stream()
                    .filter(attempt -> attempt.getUsersDTO().getLastname().toLowerCase().contains(lowerCaseQuery)
                            || attempt.getUsersDTO().getFirstname().toLowerCase().contains(lowerCaseQuery)
                            || attempt.getUsersDTO().getId().toString().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }

        return attempts;
    }

    //lấy thông tin người làm bài thi
    public AttemptViewDTO getAttemptDetailInfo(Integer attemptId, Integer courseId, Integer quizId) {
        List<QuizAttemptListDTO.AttemptDTO> attempts = getAllAttemptStudents(quizId, courseId);
        AttemptViewDTO quizAttemptListDTO = null;
        for(QuizAttemptListDTO.AttemptDTO attempt : attempts) {
            if(attempt.getId().equals(attemptId)) {
                attempt.setUsersDTO(usersService.getUserByID(attempt.getUserid()));
                System.out.println("Tn người dùng trong service: " + attempt.getUsersDTO().getFullname());
                //tính thời gian làm bài
                QuizAttemptListDTO.AttemptDTO durationAttempt = new QuizAttemptListDTO.AttemptDTO(attempt.getTimestart(), attempt.getTimefinish());
                attempt.setDuration(durationAttempt.getDurationFormat());
                if(attempt.getState().equals("finished")) {
                    attempt.setState("Hoàn thành");
                }
                // Lấy điểm chi tiết từ API mod_quiz_get_attempt_review
                String reviewUrl = domainName + "/webservice/rest/server.php"
                        + "?wstoken=" + token
                        + "&wsfunction=mod_quiz_get_attempt_review"
                        + "&moodlewsrestformat=json"
                        + "&attemptid=" + attempt.getId();
                quizAttemptListDTO = restTemplate.getForObject(reviewUrl, AttemptViewDTO.class);
                if (quizAttemptListDTO != null) {
                    quizAttemptListDTO.setAttempt(attempt);
                }
            }
        }
        return quizAttemptListDTO;
    }

    // Hàm để loại bỏ các ký tự như a., b., c. từ câu trả lời
    private String cleanResponse(String response) {
        return response.replaceAll("^[a-zA-Z]\\.\\s*", "").trim();
    }
    //lấy nội dung bài thi
    public List<QuestionDetail> getAttemptDetails(Integer attemptId) {
        List<QuestionDetail> questionDetails = new ArrayList<>();
        try {
            String getAttemptReviewFunction = "mod_quiz_get_attempt_review";
            String getAttemptReviewUrl = domainName + "/webservice/rest/server.php" +
                    "?wstoken=" + token +
                    "&wsfunction=" + getAttemptReviewFunction +
                    "&moodlewsrestformat=json" +
                    "&attemptid=" + attemptId;

            String attemptReviewResponse = restTemplate.getForObject(getAttemptReviewUrl, String.class);
            JSONObject reviewJson = new JSONObject(attemptReviewResponse);
            JSONArray questionArray = reviewJson.getJSONArray("questions");

            for (int i = 0; i < questionArray.length(); i++) {
                JSONObject questionJson = questionArray.getJSONObject(i);

                // Khởi tạo QuestionDetail với số thứ tự câu hỏi
                QuestionDetail questionDetail = new QuestionDetail(i + 1, "", "", "", new ArrayList<>(), "", false);

                // Parse HTML từ câu hỏi
                String questionHtml = questionJson.optString("html", "");
                Document doc = Jsoup.parse(questionHtml);

                // Lấy nội dung câu hỏi
                String questionText = doc.select(".qtext").text();
                String correctResponse = cleanResponse(
                        doc.select(".rightanswer").text()
                                .replace("The correct answer is: ", "")
                                .replaceAll("Đáp án chính xác là \"(.*?)\"", "$1")
                );


                // Lấy tất cả các phương án trả lời
                Elements answerElements = doc.select(".answer .r0, .answer .r1");
                List<String> allResponses = new ArrayList<>();
                String studentResponse = "";

                // Duyệt qua từng đáp án để thêm vào danh sách và kiểm tra câu trả lời của sinh viên
                for (Element answerElement : answerElements) {
                    String responseText = answerElement.text();
                    allResponses.add(cleanResponse(responseText));  // Clean trước khi lưu vào danh sách đáp án

                    // Kiểm tra nếu đây là câu trả lời sinh viên đã chọn
                    if (!answerElement.select("input[checked=checked]").isEmpty()) {
                        studentResponse = cleanResponse(responseText);  // Cắt ký tự a., b., c. trong câu trả lời sinh viên đã chọn
                    }
                }

                // So sánh câu trả lời của sinh viên với câu trả lời đúng sau khi đã xử lý
                boolean isCorrect = studentResponse.equalsIgnoreCase(correctResponse);

                // Đặt giá trị vào `questionDetail`
                questionDetail.setQuestionText(questionText);
                questionDetail.setStudentResponse(studentResponse);  // Đã được xử lý sạch
                questionDetail.setCorrectResponse(correctResponse);  // Đã được xử lý sạch
                questionDetail.setAllResponses(allResponses);  // Lưu danh sách đáp án đã được xử lý
                questionDetail.setCorrect(isCorrect);  // Đặt trạng thái đúng/sai

                System.out.println("Student Response: " + studentResponse);
                System.out.println("Correct Response: " + correctResponse);
                System.out.println("isCorrect: " + isCorrect);
                // Thêm câu hỏi vào danh sách
                questionDetails.add(questionDetail);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return questionDetails;
    }
}
