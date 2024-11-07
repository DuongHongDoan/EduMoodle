package com.example.edumoodle.DTO;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class QuizzesDTO {
    private List<QuizzesListDTO> quizzes;

    public List<QuizzesListDTO> getQuizzes() {
        return quizzes;
    }

    public void setQuizzes(List<QuizzesListDTO> quizzes) {
        this.quizzes = quizzes;
    }

    public static class QuizzesListDTO {
        private Integer id;
        private Integer coursemodule;
        private String name;
        private Integer section;
        private Integer attempts;
        private BigDecimal sumgrades;
        private BigDecimal grade;
        private Long timecreated;
        private Long timemodified;

        // Hiển thị điểm ngăn cách bằng dấu phẩy và lấy 2 số thập phân sau dấu phẩy
        public String getFormattedGrade() {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMANY);
            symbols.setDecimalSeparator(',');
            DecimalFormat df = new DecimalFormat("0.00", symbols);
            return df.format(this.grade);
        }

        public QuizzesListDTO(){}

        public QuizzesListDTO(Integer id, Integer coursemodule, String name, Integer section, Integer attempts, BigDecimal sumgrades, BigDecimal grade, Long timecreated, Long timemodified) {
            this.id = id;
            this.coursemodule = coursemodule;
            this.name = name;
            this.section = section;
            this.attempts = attempts;
            this.sumgrades = sumgrades;
            this.grade = grade;
            this.timecreated = timecreated;
            this.timemodified = timemodified;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getCoursemodule() {
            return coursemodule;
        }

        public void setCoursemodule(Integer coursemodule) {
            this.coursemodule = coursemodule;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getSection() {
            return section;
        }

        public void setSection(Integer section) {
            this.section = section;
        }

        public Integer getAttempts() {
            return attempts;
        }

        public void setAttempts(Integer attempts) {
            this.attempts = attempts;
        }

        public BigDecimal getSumgrades() {
            return sumgrades;
        }

        public void setSumgrades(BigDecimal sumgrades) {
            this.sumgrades = sumgrades;
        }

        public BigDecimal getGrade() {
            return grade;
        }

        public void setGrade(BigDecimal grade) {
            this.grade = grade;
        }

        public Long getTimecreated() {
            return timecreated;
        }

        public void setTimecreated(Long timecreated) {
            this.timecreated = timecreated;
        }

        public Long getTimemodified() {
            return timemodified;
        }

        public void setTimemodified(Long timemodified) {
            this.timemodified = timemodified;
        }
    }
}
