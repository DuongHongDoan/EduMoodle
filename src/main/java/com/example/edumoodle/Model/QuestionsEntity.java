package com.example.edumoodle.Model;
import com.example.edumoodle.Model.QuestionAnswersEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_questions")
public class QuestionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 255)
    private String name;
    private Integer moodleId;
    @Lob
    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(nullable = false, length = 50)
    private String qtype = "multichoice";
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
//    @Column(name = "category_id")
    private QuestionCategoriesEntity categoryId;


//    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<QuestionAnswersEntity> answers;
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionAnswersEntity> answers = new ArrayList<>(); // Khởi tạo tại đây

    // Constructor mặc định
    public QuestionsEntity() {
        // Đảm bảo `answers` không null
        this.answers = new ArrayList<>();
    }

    // Phương thức để thêm câu trả lời vào danh sách `answers`
    public void addAnswer(QuestionAnswersEntity answer) {
        if (this.answers == null) {
            this.answers = new ArrayList<>();
        }
        this.answers.add(answer);
        answer.setQuestion(this);
    }
    public Integer getMoodleId() {
        return moodleId;
    }

    public void setMoodleId(Integer moodleId) {
        this.moodleId = moodleId;
    }



    public void removeAnswer(QuestionAnswersEntity answer) {
        answers.remove(answer);
        answer.setQuestion(null);
    }
    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQtype() {
        return qtype;
    }

    public void setQtype(String qtype) {
        this.qtype = qtype;
    }

    public QuestionCategoriesEntity getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(QuestionCategoriesEntity categoryId) {
        this.categoryId = categoryId;
    }

    public List<QuestionAnswersEntity> getAnswers() {
        return answers;
    }

    public void setAnswers(List<QuestionAnswersEntity> answers) {
        this.answers = answers;
    }
}
