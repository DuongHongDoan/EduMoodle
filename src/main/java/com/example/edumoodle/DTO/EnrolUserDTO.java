package com.example.edumoodle.DTO;

import java.util.List;

public class EnrolUserDTO {
    private List<Integer> userid;
    private Integer courseid;
    private Integer roleid;

    public EnrolUserDTO(List<Integer> userid, Integer courseid, Integer roleid) {
        this.userid = userid;
        this.courseid = courseid;
        this.roleid = roleid;
    }

    public EnrolUserDTO() {

    }

    public List<Integer> getUserid() {
        return userid;
    }

    public void setUserid(List<Integer> userid) {
        this.userid = userid;
    }

    public Integer getCourseid() {
        return courseid;
    }

    public void setCourseid(Integer courseid) {
        this.courseid = courseid;
    }

    public Integer getRoleid() {
        return roleid;
    }

    public void setRoleid(Integer roleid) {
        this.roleid = roleid;
    }
}
