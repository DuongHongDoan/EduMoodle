package com.example.edumoodle.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {
    @GetMapping("/dashboard")
    public String getLayout() {
        return "/admin/Dashboard";
    }

    @GetMapping("/category")
    public String getCategory() {
        return "/admin/ManageCategory";
    }

    @GetMapping("/category/create-category")
    public String getCreateCate() {
        return "/admin/CreateCategory";
    }
    @GetMapping("/category/edit-category")
    public String getEditCate() {
        return "/admin/EditCategory";
    }

    @GetMapping("/courses")
    public String getListCourses() {
        return "/admin/ManageCourses";
    }
    @GetMapping("/courses/create-course")
    public String getCreateCourse() {
        return "/admin/CreateCourse";
    }
    @GetMapping("/courses/edit-course")
    public String getEditCourse() {
        return "/admin/EditCourse";
    }
    @GetMapping("/courses/detail-course")
    public String getDetailCourse() {
        return "/admin/DetailCourse";
    }
    @GetMapping("/courses/add-member")
    public String getAddMember() {
        return "/admin/AddMemberCourse";
    }
}
