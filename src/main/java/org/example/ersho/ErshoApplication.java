package org.example.ersho;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.ersho.Mapper")
public class ErshoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErshoApplication.class, args);
    }

}
