package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 查询商铺类型列表
     * @return
     */
    @Override
    public Result queryShopTypeList() {
        //从redis中查询店铺缓存
        List<String> listShopType = stringRedisTemplate.opsForList().range(RedisConstants.CACHE_SHOP_KEY + "shopType", 0, -1);
        //如果存在 返回
        if (listShopType != null && !listShopType.isEmpty()){
            List<ShopType> list = listShopType.stream()
                    .map(json -> JSONUtil.toBean(json, ShopType.class))
                    .collect(Collectors.toList());
            return Result.ok(list);
        }
        //如果不存在 从数据库查询
        List<ShopType> mysqlList = query().orderByAsc("sort").list();
        //判断数据库中是否存在
        if (mysqlList == null || mysqlList.isEmpty()){
            //不存在 返回错误信息
            return Result.fail("数据库中不存在");
        }
        //存在 写入redis
        List<String> list = mysqlList.stream()
                .map(json -> JSONUtil.toJsonStr(json))
                .collect(Collectors.toList());
        stringRedisTemplate.opsForList().rightPushAll(RedisConstants.CACHE_SHOP_KEY + "shopType", list);
        //返回
        return Result.ok(list);
    }
}
