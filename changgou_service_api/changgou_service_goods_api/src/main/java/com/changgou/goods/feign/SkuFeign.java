package com.changgou.goods.feign;

import com.changgou.entity.Result;
import com.changgou.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "goods")
public interface SkuFeign {

    //根据spuid查询sku列表
    @GetMapping("/sku/spu/{id}")
    List<Sku> findSkuListBySpuId(@PathVariable("id") String id);

    @GetMapping("/sku/{id}")
    public Result<Sku> findById(@PathVariable(value = "id") String id);

}
