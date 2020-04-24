package com.changgou.search.controller;

import com.changgou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    @ResponseBody
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

    

    //搜索页面的跳转
    @GetMapping("/list")
    public String list(@RequestParam Map<String,String> searchMap, Model model) throws Exception {
        //特殊符号的处理
        this.handelSearchMap(searchMap);

        //获取查询结果
        Map resultMap = searchService.search(searchMap);

        model.addAttribute("result",resultMap);//携带数据，传到页面
        model.addAttribute("searchMap",searchMap);

        //拼接url
        StringBuilder url = new StringBuilder("/search/list");
        if (searchMap!=null && searchMap.size()>0){
            //有查询条件
            url.append("?");
            for (String key : searchMap.keySet()) {
                if (!"sortRule".equals(key) && !"sortField".equals(key) && !"pageNum".equals(key)) {
                    url.append(key).append("=").append(searchMap.get(key)).append("&");
                }
            }
            //http://127.0.0.1:9009/search/list?keywords=手机&spec_颜色=蓝色
            String urlString = url.toString();

            //去除路径上之后一个&号
            urlString=urlString.substring(0,urlString.length()-1);

            model.addAttribute("url",urlString);
        }else {
            //没有查询条件
            String urlString = url.toString();
            model.addAttribute("url",urlString);
        }


        return "search";//直接跳转到templates目录下的search.html页面
    }
}
