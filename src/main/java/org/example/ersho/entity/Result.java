package org.example.ersho.entity;

import lombok.Data;

@Data
public class Result {
    private Object object;
    private int code;
    private String msg;


    public static Result success(Object object){
        Result result=new Result();
        result.setCode(200);
        result.setObject(object);
        result.setMsg("操作成功");
        return result;
    }
    public static Result success(){
        Result result=new Result();
        result.setCode(200);
        result.setMsg("success");
        return result;
    }

    public static Result error(String msg){
        Result result=new Result();
        result.setCode(400);
        result.setMsg(msg);
        return result;
    }

}
