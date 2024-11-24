package com.example.edumoodle.Service;

import com.example.edumoodle.Model.*;
import com.example.edumoodle.Repository.CoursesRepository;
import com.example.edumoodle.Repository.SchoolYearRepository;
import com.example.edumoodle.Repository.SemesterRepository;
import com.example.edumoodle.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

@Service
public class DashboardService {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private CoursesRepository coursesRepository;
    @Autowired
    private SchoolYearRepository schoolYearRepository;
    @Autowired
    private SemesterRepository semesterRepository;

    public Map<String, Integer> getStatistics(Integer id_school_year, Integer id_semester) {
        List<CoursesEntity> courses = coursesRepository.findCoursesBySchoolYearSemester(id_school_year, id_semester);
        List<UsersEntity> teachers = usersRepository.findTeachersBySchoolYearSemester(id_school_year, id_semester);
        List<UsersEntity> students = usersRepository.findStudentsBySchoolYearSemester(id_school_year, id_semester);

        Map<String, Integer> statistics = new HashMap<>();
        statistics.put("courseCount", courses.size());
        statistics.put("teacherCount", teachers.size());
        statistics.put("studentCount", students.size());

        return statistics;
    }

    public SchoolYearSemesterEntity getCurrentSchoolYearSemester() {
        LocalDateTime today = LocalDateTime.now();
        int currentYear = today.getYear();
        Month currentMonth = today.getMonth();

        String hocKy;
        String namHoc;

        // Xác định học kỳ và năm học dựa trên tháng hiện tại
        if (currentMonth.getValue() >= 9) {
            hocKy = "1";
            namHoc = currentYear + " - " + (currentYear + 1);
        } else if (currentMonth.getValue() <= 5) {
            hocKy = "2";
            namHoc = (currentYear - 1) + " - " + currentYear;
        } else {
            hocKy = "3";
            namHoc = (currentYear - 1) + " - " + currentYear;
        }

        SchoolYearsEntity schoolYearsEntity = getSchoolYearName(namHoc);
        SemestersEntity semestersEntity = getSemesterName(hocKy);
        if(schoolYearsEntity == null || semestersEntity == null) {
            throw new IllegalArgumentException("Không tìm thấy năm học hoặc học kỳ tương ứng trong cơ sở dữ liệu.");
        }

        return new SchoolYearSemesterEntity(schoolYearsEntity, semestersEntity);
    }

    public List<SchoolYearsEntity> getAllSchoolYear() {
        return schoolYearRepository.findAll();
    }
    public SchoolYearsEntity getSchoolYearName(String name) {
        return schoolYearRepository.findBySchoolYearName(name);
    }

    public List<SemestersEntity> getAllSemester() {
        return semesterRepository.findAll();
    }
    public SemestersEntity getSemesterName(String name) {
        return semesterRepository.findBySemesterName(name);
    }
}
