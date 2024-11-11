package com.example.edumoodle.Model;
import com.example.edumoodle.Model.QuestionAnswersEntity;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "tbl_questions")
public class QuestionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 255)
    private String name;

    @Lob
    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(nullable = false, length = 50)
    private String qtype = "multichoice";

    @Column(name = "category_id")
    private int categoryId;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionAnswersEntity> answers;


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

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public List<QuestionAnswersEntity> getAnswers() {
        return answers;
    }

    public void setAnswers(List<QuestionAnswersEntity> answers) {
        this.answers = answers;
    }
}
