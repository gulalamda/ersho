package org.example.ersho.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.example.ersho.entity.Goods;
import org.example.ersho.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    // 这里要落库的是前端提交上来的 goods，直接写回 Dbgoods 等于什么都没改
    goods.setId(Dbgoods.getId());
    goods.setSellerId(userId);
    goodsService.updateById(goods);
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
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        // 没登录时 userId 是 null，而 SQL 里 seller_id <> NULL 永远不成立，
        // 会让集市对游客显示成空的。所以只有登录了才排除自己的商品。
        if (userId != null) {
            wrapper.ne(Goods::getSellerId, userId);
        }
        return Result.success(goodsService.list(wrapper));
    }





}


