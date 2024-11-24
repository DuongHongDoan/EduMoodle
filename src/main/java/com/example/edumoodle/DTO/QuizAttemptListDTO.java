package com.example.edumoodle.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class QuizAttemptListDTO {
    private BigDecimal grade;
    private List<AttemptDTO> attempts;

    public BigDecimal getGrade() {
        return grade;
    }

    public void setGrade(BigDecimal grade) {
        this.grade = grade;
    }

    public List<AttemptDTO> getAttempts() {
        return attempts;
    }

    public void setAttempts(List<AttemptDTO> attempts) {
        this.attempts = attempts;
    }
    public static class AttemptDTO {
        private Integer id;
        private Integer quiz;
        private Integer userid;
        private Integer attempt;
        private Integer uniqueid;
        private String state;
        private Long timestart;
        private Long timefinish;
        private Long timemodified;
        private String duration;
        private BigDecimal grade;
        private BigDecimal sumgrades;
        @JsonIgnore
        private UsersDTO usersDTO;

        public AttemptDTO(){}

        public AttemptDTO(Integer id, Integer quiz, Integer userid, Integer attempt, Integer uniqueid, String state, Long timestart, Long timefinish, Long timemodified) {
            this.id = id;
            this.quiz = quiz;
            this.userid = userid;
            this.attempt = attempt;
            this.uniqueid = uniqueid;
            this.state = state;
            this.timestart = timestart;
            this.timefinish = timefinish;
            this.timemodified = timemodified;
        }

        public String getTimestartAsLocalDateTime() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, 'ngày' d 'tháng' M 'năm' yyyy, h:mm a", new Locale("vi", "VN"));
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestart), ZoneId.systemDefault());
            return dateTime.format(formatter).replace("SA", "AM").replace("CH", "PM");
        }

        public String getTimefinishAsLocalDateTime() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, 'ngày' d 'tháng' M 'năm' yyyy, h:mm a", new Locale("vi", "VN"));
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timefinish), ZoneId.systemDefault());
            return dateTime.format(formatter).replace("SA", "AM").replace("CH", "PM");
        }

        //tính thời gian làm bài thi
        public AttemptDTO(Long timestart, Long timefinish) {
            this.timestart = timestart;
            this.timefinish = timefinish;
        }

        public String getDurationFormat() {
            // Chuyển timestart và timefinish từ giây sang LocalDateTime
            LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestart), ZoneId.systemDefault());
            LocalDateTime finish = LocalDateTime.ofInstant(Instant.ofEpochSecond(timefinish), ZoneId.systemDefault());
            // Tính Duration giữa start và finish
            Duration duration = Duration.between(start, finish);
            Long hours = duration.toHours();
            Long minutes = duration.toMinutes() % 60;
            Long seconds = duration.getSeconds() % 60;

            // Format kết quả thành chuỗi
            StringBuilder result = new StringBuilder();
            if (hours > 0) {
                result.append(hours).append(" tiếng ");
            }
            if (minutes > 0) {
                result.append(minutes).append(" phút ");
            }
            if (seconds > 0 || (hours == 0 && minutes == 0)) {
                result.append(seconds).append(" giây ");
            }

            return result.toString().trim();
        }

        // Hiển thị điểm ngăn cách bằng dấu phẩy và lấy 2 số thập phân sau dấu phẩy
        public String getFormattedGrade() {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMANY);
            symbols.setDecimalSeparator(',');
            DecimalFormat df = new DecimalFormat("0.00", symbols);
            return df.format(this.grade);
        }
        public String getFormattedSumGrade() {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMANY);
            symbols.setDecimalSeparator(',');
            DecimalFormat df = new DecimalFormat("0.00", symbols);
            return df.format(this.sumgrades);
        }

        public BigDecimal getSumgrades() {
            return sumgrades;
        }

        public void setSumgrades(BigDecimal sumgrades) {
            this.sumgrades = sumgrades;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public BigDecimal getGrade() {
            return grade;
        }

        public void setGrade(BigDecimal grade) {
            this.grade = grade;
        }

        public UsersDTO getUsersDTO() {
            return usersDTO;
        }

        public void setUsersDTO(UsersDTO usersDTO) {
            this.usersDTO = usersDTO;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getQuiz() {
            return quiz;
        }

        public void setQuiz(Integer quiz) {
            this.quiz = quiz;
        }

        public Integer getUserid() {
            return userid;
        }

        public void setUserid(Integer userid) {
            this.userid = userid;
        }

        public Integer getAttempt() {
            return attempt;
        }

        public void setAttempt(Integer attempt) {
            this.attempt = attempt;
        }

        public Integer getUniqueid() {
            return uniqueid;
        }

        public void setUniqueid(Integer uniqueid) {
            this.uniqueid = uniqueid;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public Long getTimestart() {
            return timestart;
        }

        public void setTimestart(Long timestart) {
            this.timestart = timestart;
        }

        public Long getTimefinish() {
            return timefinish;
        }

        public void setTimefinish(Long timefinish) {
            this.timefinish = timefinish;
        }

        public Long getTimemodified() {
            return timemodified;
        }

        public void setTimemodified(Long timemodified) {
            this.timemodified = timemodified;
        }
    }
}
