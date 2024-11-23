package com.example.edumoodle.Controller.admin;

import com.example.edumoodle.DTO.CategoriesDTO;
import com.example.edumoodle.DTO.CoursesDTO;
import com.example.edumoodle.DTO.UsersDTO;
import com.example.edumoodle.Model.SchoolYearSemesterEntity;
import com.example.edumoodle.Model.SchoolYearsEntity;
import com.example.edumoodle.Model.SemestersEntity;
import com.example.edumoodle.Model.UsersEntity;
import com.example.edumoodle.Repository.UsersRepository;
import com.example.edumoodle.Service.CategoriesService;
import com.example.edumoodle.Service.CoursesService;
import com.example.edumoodle.Service.DashboardService;
import com.example.edumoodle.Service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private CoursesService coursesService;
    @Autowired
    private CategoriesService categoriesService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/admin/dashboard")
    public String getDashboard(Model model) {
        SchoolYearSemesterEntity currentNamHocHocKy = dashboardService.getCurrentSchoolYearSemester();
        Integer namHocId = currentNamHocHocKy.getSchoolYearsEntity().getId_school_year();
        Integer hocKyId = currentNamHocHocKy.getSemestersEntity().getId_semester();

        // Lấy thống kê theo năm học và học kỳ hiện tại
        Map<String, Integer> statistics = dashboardService.getStatistics(namHocId, hocKyId);
        model.addAttribute("courseCnt", statistics.get("courseCount"));
        model.addAttribute("teacher", statistics.get("teacherCount"));
        model.addAttribute("student", statistics.get("studentCount"));

        List<SchoolYearsEntity> schoolYears = dashboardService.getAllSchoolYear();
        model.addAttribute("schoolYears", schoolYears);
        List<SemestersEntity> semesters = dashboardService.getAllSemester();
        model.addAttribute("semesters", semesters);

        model.addAttribute("currentSchoolYear", namHocId);
        model.addAttribute("currentSemester", hocKyId);

        List<CategoriesDTO> cateTest = categoriesService.getAllCategory();
        categoriesService.saveCategories(cateTest);
        return "admin/Dashboard";
    }

    @GetMapping("/admin/filter")
    public String getStatisticsFilter(@RequestParam("id_school_year") Integer id_school_year,
                                @RequestParam("id_semester") Integer id_semester,
                                Model model) {
        Map<String, Integer> statistics = dashboardService.getStatistics(id_school_year, id_semester);
        model.addAttribute("courseCnt", statistics.get("courseCount"));
        model.addAttribute("teacher", statistics.get("teacherCount"));
        model.addAttribute("student", statistics.get("studentCount"));

        List<SchoolYearsEntity> schoolYears = dashboardService.getAllSchoolYear();
        model.addAttribute("schoolYears", schoolYears);
        List<SemestersEntity> semesters = dashboardService.getAllSemester();
        model.addAttribute("semesters", semesters);

        //truyền lại nội dung loc
        model.addAttribute("currentSchoolYear", id_school_year);
        model.addAttribute("currentSemester", id_semester);
        return "admin/Dashboard";
    }
}
