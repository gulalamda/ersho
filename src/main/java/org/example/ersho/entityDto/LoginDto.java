package org.example.ersho.entityDto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginDto implements Serializable {

    private String name;

    private String password;
}
