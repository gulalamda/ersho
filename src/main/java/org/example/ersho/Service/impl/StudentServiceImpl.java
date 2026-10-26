package org.example.ersho.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ersho.entityDto.LoginDto;
import org.example.ersho.entity.Student;
import org.example.ersho.entityVo.LoginVo;
import org.example.ersho.mapper.StudentMapper;
import org.example.ersho.Service.IStudentService;
import org.example.ersho.exception.BizException;
import org.example.ersho.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements IStudentService {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public LoginVo login(LoginDto loginDto) {
        
        // 原来用的是 .one()：库里只要有两条同名记录就会直接抛异常（500）。
        // 这里改成取同名记录里密码对得上的那条，历史脏数据也不会把登录搞挂。
        Student student = lambdaQuery()
                .eq(Student::getName, loginDto.getName())
                .list()
                .stream()
                .filter(s -> s.getPassword() != null
                        && passwordEncoder.matches(loginDto.getPassword(), s.getPassword()))
                .findFirst()
                .orElse(null);

        if (student == null) {
            throw new BizException("用户名或密码错误");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", student.getId());
        claims.put("name", student.getName());

        LoginVo vo = new LoginVo();
        vo.setId(student.getId());
        vo.setName(student.getName());
        vo.setJwt(JwtUtil.getToken(claims));
        return vo;
    }

}
