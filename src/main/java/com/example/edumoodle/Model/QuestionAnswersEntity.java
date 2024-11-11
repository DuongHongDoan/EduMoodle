package com.example.edumoodle.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_questions_answers")
public class QuestionAnswersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private com.example.edumoodle.Model.QuestionsEntity question;

    @Lob
    @Column(name = "answer_text", nullable = false)
    private String answerText;

    @Column(name = "correct", nullable = false)
    private boolean correct;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public QuestionsEntity getQuestion() {
        return question;
    }

    public void setQuestion(QuestionsEntity question) {
        this.question = question;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public boolean correct() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}

