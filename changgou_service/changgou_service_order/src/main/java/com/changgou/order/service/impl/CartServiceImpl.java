package com.changgou.order.service.impl;

import com.changgou.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    private static final String CART="cart_";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;


    /**
     * 添加购物车
     * @param skuId
     * @param num
     * @param username
     */
    @Override
    public void addCart(String skuId, Integer num, String username) {

        //1.查询redis中相对应的商品信息
        //orderItem 即是：购物车中的每一个购物项
        OrderItem orderItem = (OrderItem) redisTemplate.boundHashOps(CART + username).get(skuId);

        if (orderItem!=null){
            //2.如果当前商品在redis中存在，已经添加过。则更新商品的数量和价钱
            orderItem.setNum(orderItem.getNum()+num);
            if (orderItem.getNum()<=0){
                //删除该商品
                redisTemplate.boundHashOps(CART+username).delete(skuId);
                return;
            }
            orderItem.setMoney(orderItem.getNum()*orderItem.getPrice());
            orderItem.setPayMoney(orderItem.getNum()*orderItem.getPrice());
        }else {
            //3.如果当前的商品在redis中不存在，将商品添加到redis中
            Result<Sku> skuResult = skuFeign.findById(skuId);
            Sku sku = skuResult.getData();
            Spu spu = spuFeign.findSpuById(sku.getSpuId()).getData();

            //封装orderItem
            orderItem = this.Sku2OrderItem(sku,spu,num);

        }

        //4.将orderItem添加到redis中
        redisTemplate.boundHashOps(CART+username).put(skuId,orderItem);

    }

    /**
     * 查询购物车列表数据
     * @param username
     * @return
     */
    @Override
    public Map list(String username) {
        Map map = new HashMap<>();

        List<OrderItem> orderItemList = redisTemplate.boundHashOps(CART + username).values();
        map.put("orderItemList",orderItemList);

        //商品的总数量和总价格
        Integer totalNum=0;
        Integer totalMoney=0;

        for (OrderItem orderItem : orderItemList) {
            totalNum += orderItem.getNum();
            totalMoney += orderItem.getMoney();
        }

        map.put("totalNum",totalNum);
        map.put("totalMoney",totalMoney);

        return map;
    }

    private OrderItem Sku2OrderItem(Sku sku, Spu spu, Integer num) {
        OrderItem orderItem = new OrderItem();

        orderItem.setSpuId(sku.getSpuId());
        orderItem.setSkuId(sku.getId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(orderItem.getPrice()*num);
        orderItem.setPayMoney(orderItem.getPrice()*num);
        orderItem.setImage(sku.getImage());
        orderItem.setWeight(sku.getWeight()*num);
        //当前商品的分类信息
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());


        return orderItem;
    }
}
