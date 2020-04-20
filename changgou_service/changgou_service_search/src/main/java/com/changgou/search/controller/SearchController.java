package com.changgou.search.controller;

import com.changgou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public Map search(@RequestParam Map<String,String> searchMap){
        try {
            //特殊符号的处理
            this.handelSearchMap(searchMap);
                Map search = searchService.search(searchMap);
            return search;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //特殊符号的处理
    private void handelSearchMap(Map<String, String> searchMap) {
        Set<Map.Entry<String, String>> entries = searchMap.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            if (entry.getKey().startsWith("spec_")){
                searchMap.put(entry.getKey(),entry.getValue().replace("+","%2B"));
            }
        }
    }

}
