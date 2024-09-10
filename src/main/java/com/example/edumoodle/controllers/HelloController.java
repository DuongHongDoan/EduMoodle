package com.example.edumoodle.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
