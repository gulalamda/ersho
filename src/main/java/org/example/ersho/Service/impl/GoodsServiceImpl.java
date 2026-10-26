package org.example.ersho.Service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.ersho.entity.Goods;
import org.example.ersho.entity.Result;
import org.example.ersho.mapper.GoodsMapper;
import org.example.ersho.Service.IGoodsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 学生商品表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-10-04
 */
@Slf4j
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    @Override
    public Result Selectgoods(String name) {
        List<Goods> goodsList = lambdaQuery()
                .like(Goods::getName, name)
                .list();

        if (goodsList.isEmpty()) {
            return Result.error("没有该商品");
        }
        return Result.success(goodsList);

    }

    }


