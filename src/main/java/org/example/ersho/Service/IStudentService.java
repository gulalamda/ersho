package org.example.ersho.Service;

import org.example.ersho.entityDto.LoginDto;
import org.example.ersho.entity.Student;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ersho.entityVo.LoginVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2026-10-02
 */
public interface IStudentService extends IService<Student> {

    LoginVo login(LoginDto loginDto);


}
