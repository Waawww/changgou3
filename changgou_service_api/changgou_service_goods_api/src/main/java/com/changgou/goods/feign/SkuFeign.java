package com.changgou.goods.feign;

import com.changgou.entity.Result;
import com.changgou.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "goods")
public interface SkuFeign {

    //根据spuid查询sku列表
    @GetMapping("/sku/spu/{id}")
    List<Sku> findSkuListBySpuId(@PathVariable("id") String id);

    @GetMapping("/sku/{id}")
    public Result<Sku> findById(@PathVariable(value = "id") String id);

    @PostMapping("/sku/decr/count")
    public Result decrCount(@RequestParam(value = "username") String username);

    //回滚库存，扣除销量
    @RequestMapping("/sku/resumeStockNum")
    public Result resumeStockNum(@RequestParam("skuId") String skuId,@RequestParam("num") Integer num);

}
