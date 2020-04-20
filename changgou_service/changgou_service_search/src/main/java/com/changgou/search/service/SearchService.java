package com.changgou.search.service;

import java.util.Map;

public interface SearchService {
    /**
     * 全文检索
     * @param searchMap 查询参数
     * @return
     */
    //按照查询条件进行数据查询
    public Map search(Map<String, String> searchMap) throws Exception;

}
