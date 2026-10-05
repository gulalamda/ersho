package org.example.ersho;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;

@SpringBootApplication
@MapperScan("org.example.ersho.Mapper")
@ServletComponentScan
public class ErshoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErshoApplication.class, args);
    }

}
