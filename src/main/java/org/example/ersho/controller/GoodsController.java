package org.example.ersho.controller;


import org.example.ersho.entityDto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 学生商品表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-10-04
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private org.example.ersho.Service.IGoodsService goodsService;
    //查商品
    @GetMapping("/select")
    public Result Selectgoods(@RequestParam String name){
        return goodsService.Selectgoods(name);
    }
    //买商品 detel
    //上架商品 insert

}
