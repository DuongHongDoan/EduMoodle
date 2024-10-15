package com.example.edumoodle.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ForumDiscussionsDTO {
    private Integer id;
    private String subject;
    private String message;
    private Integer parent;
    private Integer userid;
    private String userfullname;
    private String created;
    private String modified;
    private String timemodified;
    private String name;

    public ForumDiscussionsDTO() {}

    public ForumDiscussionsDTO(Integer id, String subject, String message, Integer parent, Integer userid, String userfullname, String created, String modified, String timemodified) {
        this.id = id;
        this.subject = subject;
        this.message = message;
        this.parent = parent;
        this.userid = userid;
        this.userfullname = userfullname;
        this.created = created;
        this.modified = modified;
        this.timemodified = timemodified;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public String getUserfullname() {
        return userfullname;
    }

    public void setUserfullname(String userfullname) {
        this.userfullname = userfullname;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getTimemodified() {
        return timemodified;
    }

    public void setTimemodified(String timemodified) {
        this.timemodified = timemodified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
