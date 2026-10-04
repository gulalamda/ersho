package org.example.ersho.Service.impl;

import org.example.ersho.EntityDto.LoginDto;
import org.example.ersho.EntityDto.Student;
import org.example.ersho.EntityVo.LoginVo;
import org.example.ersho.Mapper.StudentMapper;
import org.example.ersho.Service.IStudentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ersho.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements IStudentService {

    @Override
    public LoginVO login(LoginRequest request) {
        Student student = lambdaQuery()
                .eq(Student::getName, request.getName())
                .one();
        if (student == null || !passwordEncoder.matches(request.getPassword(), student.getPassword())) {
            throw new BizException("用户名或密码错误");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", student.getId());
        claims.put("name", student.getName());

        LoginVO vo = new LoginVO();
        vo.setId(student.getId());
        vo.setName(student.getName());
        vo.setJwt(JwtUtil.getToken(claims));
        return vo;
    }
    }

}
