package org.example.ersho.Controller;



import org.example.ersho.EntityDto.LoginDto;
import org.example.ersho.EntityDto.Result;
import org.example.ersho.EntityDto.Student;
import org.example.ersho.EntityVo.LoginVo;
import org.example.ersho.Service.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
public class StudentController {
    @Autowired
    private IStudentService studentService;

    @PostMapping("/Login")
    public Result Login(@RequestBody LoginDto loginDto){
        java.lang.String jwtLogin =studentService.Login(loginDto);
        return Result.success(jwtLogin);
    }


}
