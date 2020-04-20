package com.changgou.search.service;

public interface ESManagerService {

    //创建索引库的结构
    void creatMappingAndIndex();
    //导入全部数据进入ES
    void importAll();
    //根据spuId查询skuList，然后再导入索引库
    void importDataBySpuId(String spuId);

    //根据spuid，删除es索引库中相关的sku数据
    void delDataBySpuId(String spuId);
}
