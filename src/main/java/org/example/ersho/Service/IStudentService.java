package org.example.ersho.Service;

import org.example.ersho.EntityDto.LoginDto;
import org.example.ersho.EntityDto.Student;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ersho.EntityVo.LoginVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2026-10-02
 */
public interface IStudentService extends IService<Student> {

    String Login(LoginDto);
}
