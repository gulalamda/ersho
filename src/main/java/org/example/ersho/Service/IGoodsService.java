package org.example.ersho.Service;

import org.example.ersho.entityDto.Result;
import org.example.ersho.entity.Goods;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 学生商品表 服务类
 * </p>
 *
 * @author author
 * @since 2026-10-04
 */
public interface IGoodsService extends IService<Goods> {

    Result Selectgoods(String name);
}
