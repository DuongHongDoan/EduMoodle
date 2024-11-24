package com.example.edumoodle.DTO;

import java.math.BigDecimal;
import java.util.List;

public class AttemptViewDTO {
    private BigDecimal grade;
    private QuizAttemptListDTO.AttemptDTO attempt;
    private List<QuestionsDetail> questions;

    public BigDecimal getGrade() {
        return grade;
    }

    public void setGrade(BigDecimal grade) {
        this.grade = grade;
    }

    public QuizAttemptListDTO.AttemptDTO getAttempt() {
        return attempt;
    }

    public void setAttempt(QuizAttemptListDTO.AttemptDTO attempt) {
        this.attempt = attempt;
    }

    public List<QuestionsDetail> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionsDetail> questions) {
        this.questions = questions;
    }

    public static class QuestionsDetail {
        private Integer slot;
        private String type;
        private String questionnumber;
        private Integer number;
        private String state;
        private String stateclass;
        private String status;
        private String mark;
        private Integer maxmark;
        private String html;

        private QuestionsDetail() {}

        public QuestionsDetail(Integer slot, String type, String questionnumber, String state, Integer number, String stateclass, String status, String mark, Integer maxmark, String html) {
            this.slot = slot;
            this.type = type;
            this.questionnumber = questionnumber;
            this.state = state;
            this.number = number;
            this.stateclass = stateclass;
            this.status = status;
            this.mark = mark;
            this.maxmark = maxmark;
            this.html = html;
        }

        public Integer getSlot() {
            return slot;
        }

        public void setSlot(Integer slot) {
            this.slot = slot;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getQuestionnumber() {
            return questionnumber;
        }

        public void setQuestionnumber(String questionnumber) {
            this.questionnumber = questionnumber;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getStateclass() {
            return stateclass;
        }

        public void setStateclass(String stateclass) {
            this.stateclass = stateclass;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMark() {
            return mark;
        }

        public void setMark(String mark) {
            this.mark = mark;
        }

        public Integer getMaxmark() {
            return maxmark;
        }

        public void setMaxmark(Integer maxmark) {
            this.maxmark = maxmark;
        }

        public String getHtml() {
            return html;
        }

        public void setHtml(String html) {
            this.html = html;
        }
    }
}
