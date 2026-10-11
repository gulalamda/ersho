package org.example.ersho.controller;


import javax.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.Data;
import org.example.ersho.Service.IGoodsService;
import org.example.ersho.Service.IOrdersService;
import org.example.ersho.entity.Goods;
import org.example.ersho.entity.Orders;
import org.example.ersho.entityDto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.server.authentication.AnonymousAuthenticationWebFilter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.example.ersho.util.JwtUtil.parseToken;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-10-07
 */
@RestController
@RequestMapping("/orders")
public class OrdersController {
    @Autowired
    private IOrdersService ordersService;
    @Autowired
    private IGoodsService goodsService;
    //记录买卖

    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result insert(@RequestBody Orders orders, HttpServletRequest request) {
        Integer buyerId = (Integer) request.getAttribute("userId");
        if (orders.getGoodsId() == null) {
            return Result.error("商品id不能为空");
        }
        Goods goods = goodsService.getById(orders.getGoodsId());
        if (goods == null) {
            return Result.error("该商品不存在");
        }
        if (Objects.equals(goods.getSellerId(), buyerId)) {
            return Result.error("不能购买自己发布的商品");
        }
        int num = orders.getNum() == null ? 1 : orders.getNum();
        if (num <= 0) {
            return Result.error("购买数量不合法");
        }


        boolean deducted = goodsService.update(new LambdaUpdateWrapper<Goods>()
                .eq(Goods::getId, goods.getId())
                .ge(Goods::getStock, num)
                .setSql("stock = stock - {0}", num));
        if (!deducted) {
            return Result.error("库存不足");
        }

        LocalDateTime now = LocalDateTime.now();
        orders.setSellerId(goods.getSellerId());
        orders.setNum(num);
        orders.setPrice(goods.getPrice().multiply(BigDecimal.valueOf(num)));
        orders.setBuyerId(buyerId);
        orders.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        orders.setStatus(0);
        orders.setCreateTime(now);
        orders.setUpdateTime(now);
        ordersService.save(orders);
        return Result.success(orders);
    }






    //订单详情。
    @GetMapping("/mine")
    public Result xx(HttpServletRequest request){
        Integer userId = (Integer) request.getAttribute("userId");
        List<Orders> ordersList=ordersService.lambdaQuery().eq(Orders::getBuyerId,userId).list();
        return Result.success(ordersList);
    }
    @GetMapping("/sold")
    public Result sold(HttpServletRequest request){
        Integer userId = (Integer) request.getAttribute("userId");
        List<Orders> ordersList=ordersService.lambdaQuery().eq(Orders::getSellerId,userId).list();
        return Result.success(ordersList);
    }






    //卖/买家修改状态
    @PutMapping("/status")
    public Result updateStatus(@RequestParam Integer id,@RequestParam Integer status, HttpServletRequest request){
        Integer sellerId = (Integer) request.getAttribute("userId");
        Orders orders = ordersService.getById(id);
        if (orders==null){
            return Result.error("没有该订单");
        }
        if (Integer.valueOf(1).equals(status)) {
            if (!Objects.equals(orders.getSellerId(), sellerId)) return Result.error("只有卖家可以发货");
            if (!Integer.valueOf(0).equals(orders.getStatus())) return Result.error("该订单当前状态不能发货");
        } else if (Integer.valueOf(2).equals(status)) {
            if (!Objects.equals(orders.getBuyerId(), sellerId)) return Result.error("只有买家可以确认收货");
            if (!Integer.valueOf(1).equals(orders.getStatus())) return Result.error("卖家还没发货");
        } else {
            return Result.error("状态值不合法");
        }
        orders.setStatus(status);
        ordersService.updateById(orders);
        return Result.success("当前已发货");
    }



}
