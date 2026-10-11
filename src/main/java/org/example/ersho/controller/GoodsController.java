package org.example.ersho.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.ersho.entity.Goods;
import org.example.ersho.entityDto.Result;
import org.example.ersho.entityDto.Student;
import org.example.ersho.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 学生商品表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-10-04
 */
@Slf4j
@RestController
@RequestMapping("/goods")
public class GoodsController {
    //goods/mai?id=5,nuben=5
    @Autowired
    private org.example.ersho.Service.IGoodsService goodsService;

    //name查商品
    @GetMapping("/select")
    public Result Selectgoods(@RequestParam String name){

        return goodsService.Selectgoods(name);
    }
//改



    //上架商品 insert
    @PutMapping("/put")
    public Result put(@RequestBody Goods goods,HttpServletRequest request){
        Integer userId=(Integer) request.getAttribute("userId");
        goods.setSellerId(userId);

        goodsService.save(goods);
        return Result.success(goodsService.save(goods));
    }
    //token来查看自己的商品
    @GetMapping("/mine")
    public Result mie(HttpServletRequest request){
       Integer userId=(Integer) request.getAttribute("userId");
        return Result.success(goodsService.list(new LambdaQueryWrapper<Goods>().eq(Goods::getSellerId,userId)));
    }
    //改
    @PutMapping("/updatagood")
    public Result putgood(@RequestBody Goods goods,HttpServletRequest request){
        Integer userId = (Integer) request.getAttribute("userId");
        Goods Dbgoods=goodsService.getById(goods.getId());
    if (Dbgoods==null){
        return Result.error("商品不存在");
    }
    if (!Objects.equals(Dbgoods.getSellerId(),userId)){
        return Result.error("不是本人商品");
    }
    goodsService.updateById(Dbgoods);
        return Result.success("修改成功");
    }
    @DeleteMapping("/deleteGoods")
    public Result deleteGoods(@RequestParam Integer id,HttpServletRequest request){
        Integer userId = (Integer) request.getAttribute("userId");
        Goods goods=goodsService.getById(id);
        if(!Objects.equals(goods.getSellerId(), userId)){
            return Result.error("无权删除别人的商品");
        }
        goodsService.removeById(goods);
        return Result.success("成功下架");
    }

    //token去除自己的商品,不给看
    @GetMapping("/mina")
    public Result mina(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return Result.success(goodsService.list(new LambdaQueryWrapper<Goods>().ne(Goods::getSellerId,userId)));
    }





}


