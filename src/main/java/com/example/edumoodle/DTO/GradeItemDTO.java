package com.example.edumoodle.DTO;

public class GradeItemDTO {
    private String itemName;
    private Double grade;
    private Double maxGrade;
    private String percentage;
    private String weight; // Calculated Weight
    private String range;
    private String feedback;
    private String contributionToCourseTotal;

    // Constructor
    public GradeItemDTO(String itemName, Double grade, Double maxGrade, String percentage,
                        String weight, String range, String feedback, String contributionToCourseTotal) {
        this.itemName = itemName;
        this.grade = grade;
        this.maxGrade = maxGrade;
        this.percentage = percentage;
        this.weight = weight;
        this.range = range;
        this.feedback = feedback;
        this.contributionToCourseTotal = contributionToCourseTotal;
    }

    // Getters and Setters


    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public Double getMaxGrade() {
        return maxGrade;
    }

    public void setMaxGrade(Double maxGrade) {
        this.maxGrade = maxGrade;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getContributionToCourseTotal() {
        return contributionToCourseTotal;
    }

    public void setContributionToCourseTotal(String contributionToCourseTotal) {
        this.contributionToCourseTotal = contributionToCourseTotal;
    }
}
