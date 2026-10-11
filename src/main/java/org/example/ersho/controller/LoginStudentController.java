package org.example.ersho.controller;



import org.example.ersho.config.SecurityConfig;
import org.example.ersho.entityDto.LoginDto;
import org.example.ersho.entityDto.Result;
import org.example.ersho.Service.IStudentService;
import org.example.ersho.entityDto.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

// （/student/login
@RestController
@RequestMapping("/student")
public class LoginStudentController {
    @Autowired
    private IStudentService studentService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    //新增账号
    @PostMapping("/denlu")
    public Result denlu(@RequestBody Student student){
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        studentService.save(student);
    return Result.success(student);
    }


    @PostMapping("/login")
    public Result login(@RequestBody LoginDto loginDto){

        return Result.success(studentService.login(loginDto));
    }


    //查看用户信息
    @GetMapping("/info")
    public Result getInfo(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Student student = studentService.getById(userId);
        if (student == null) {
            return Result.error("用户不存在");
        }
        student.setPassword(null);
        return Result.success(student);
    }
    //
    @DeleteMapping("Delete")
    public Result zhuxiao(HttpServletRequest request){
        Integer userId = (Integer) request.getAttribute("userId");
        studentService.removeById(userId);
        return Result.success("注销成功");
    }
    //改
    @PutMapping("/put")
    public Result put(@RequestBody Student student,HttpServletRequest request){
        Integer userId = (Integer) request.getAttribute("userId");
        student.setId(userId);
        studentService.updateById(student);
        return Result.success("修改成功");
    }



}
