package org.example.ersho.controller;



import org.example.ersho.entityDto.LoginDto;
import org.example.ersho.entityDto.Result;
import org.example.ersho.Service.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
public class LoginStudentController {
    @Autowired
    private IStudentService studentService;

    @PostMapping("/login")
    public Result login(@RequestBody LoginDto loginDto){
        return Result.success(studentService.login(loginDto));
    }

}
