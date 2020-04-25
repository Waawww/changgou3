package com.changgou.goods.feign;

import com.changgou.entity.Result;
import com.changgou.goods.pojo.Category;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "goods")//商品服务goods
public interface CategoryFeign {

    //根据分类id查询分类信息
    @GetMapping("/category/{id}")
    public Result<Category> findById(@PathVariable("id") Integer id);

}
