package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SearchService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * //按照查询条件进行数据查询
     *
     * @param searchMap
     * @return
     * @throws Exception
     */
    @Override
    public Map search(Map<String, String> searchMap) throws Exception {

        HashMap<String, Object> resultMap = new HashMap<>();

        //构建查询
        if (searchMap != null) {
            //构建查询条件封装对象
            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            //按照关键字查询,对条件的封装
            if (StringUtils.isNotEmpty(searchMap.get("keywords"))) {
                //matchQuery是模糊查询
                boolQuery.must(QueryBuilders.matchQuery("name", searchMap.get("keywords")).operator(Operator.AND));
            }

            //按照品牌进行过滤查询
            if (StringUtils.isNotEmpty(searchMap.get("brand"))) {
                //termQuery是完全匹配查询，精确查询
                //brandName 是ES的索引库中的一个域名
                boolQuery.filter(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
            }

            //按照规格进行过滤查询
            for (String key : searchMap.keySet()) {
                if (key.startsWith("spec_")) {

                    String value = searchMap.get(key).replace("%2B", "+");
                    //key:spec_网络制式  需要裁剪
                    //域名是  specMap.网络制式.keyword
                    boolQuery.filter(QueryBuilders.termQuery(("specMap." + key.substring(5) + ".keyword"), value));
                }

            }

            //按照价格进行区间过滤查询
            if (StringUtils.isNotEmpty(searchMap.get("price"))) {
                String[] prices = searchMap.get("price").split("-");
                if (prices.length == 2) {
                    //价格是1000-2000  0-500  .lte是小于等于
                    boolQuery.filter(QueryBuilders.rangeQuery("price").lte(prices[1]));

                }
                //  .gte是大于等于
                boolQuery.filter(QueryBuilders.rangeQuery("price").gte(prices[0]));

            }


            nativeSearchQueryBuilder.withQuery(boolQuery);

            //按照品牌进行分组（聚合）查询
            String skuBrand = "skuBrand";
            //skuBrand 是聚合结果的列名
            //field 是对哪一个域进行分组查询
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuBrand).field("brandName"));


            //按照规格进行聚合查询
            String skuSpec = "skuSpec";
            //terms(skuSpec) 是进行分组查询后分组的字段列
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuSpec).field("spec.keyword"));

            //分页查询
            String pageNum = searchMap.get("pageNum");//当前页
            String pageSize = searchMap.get("pageSize");//每页显示多少条
            if (StringUtils.isEmpty(pageNum)) {
                pageNum = "1";
            }
            if (StringUtils.isEmpty(pageSize)) {
                pageSize = "30";
            }
            //设置分页
            //PageRequest.of（当前页(从0开始)，每页显示多少条）;
            nativeSearchQueryBuilder.withPageable(PageRequest.of(Integer.parseInt(pageNum) - 1, Integer.parseInt(pageSize)));

            //按照相关字段进行排序查询
            //1.当前域   ->searchMap.get("sortField")
            // 2.当前的排序操作是 升序ASC 还是 降序DESC   ->searchMap.get("sortRule")
            if (!StringUtils.isEmpty(searchMap.get("sortField")) && StringUtils.isNotEmpty(searchMap.get("sortRule"))) {
                if ("ASC".equals(searchMap.get("sortRule"))) {
                    //升序
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(searchMap.get("sortField")).order(SortOrder.ASC));
                } else {
                    //降序
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(searchMap.get("sortField")).order(SortOrder.DESC));
                }
            }


            //设置高亮域以及高亮的样式
            //高亮域  field
            //高亮样式的前缀preTags  高亮样式的后缀postTags
            HighlightBuilder.Field field = new HighlightBuilder.Field("name")
                    .preTags("<span style='color:red'>")
                    .postTags("</span>");
            nativeSearchQueryBuilder.withHighlightFields(field);


            //开启查询
            /*
             * 第一个参数：条件构建对象
             * 第二个参数：查询操作实体类
             * 第三个参数：查询结果操作对象
             * */
            //封装查询结果  resultInfo
            AggregatedPage<SkuInfo> resultInfo = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                    //查询结果的相关操作
                    List<T> list = new ArrayList<>();
                    //获取查询命中结果数据
                    SearchHits hits = searchResponse.getHits();
                    if (hits != null) {
                        //有查询结果  SearchHit就是索引库中查到的每一条记录，每一个document
                        for (SearchHit hit : hits) {
                            //searchHit对象转为skuinfo对象
                            SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);

                            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                            if (highlightFields != null && highlightFields.size() > 0) {
                                //替换数据。把没有高亮的数据，替换为高亮的数据
                                skuInfo.setName(highlightFields.get("name").getFragments()[0].toString());
                            }

                            list.add((T) skuInfo);
                        }


                    }
                    return new AggregatedPageImpl<T>(list, pageable, hits.getTotalHits(), searchResponse.getAggregations());
                }
            });

            //封装查询结果
            //总记录数
            resultMap.put("total", resultInfo.getTotalElements());
            //总页数
            resultMap.put("totalPages", resultInfo.getTotalPages());
            //数据集合
            resultMap.put("rows", resultInfo.getContent());

            //封装品牌的分组结果
            StringTerms brandTerms = (StringTerms) resultInfo.getAggregation(skuBrand);
            //流运算
            List<String> brandList = brandTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            resultMap.put("brandList", brandList);


            //封装规格的分组结果
            StringTerms specTerms = (StringTerms) resultInfo.getAggregation(skuSpec);
            List<String> specList = specTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            resultMap.put("specList", this.formartSpec(specList));

            //返回当前页
            resultMap.put("pageNum", pageNum);

            return resultMap;

        }


        return null;
    }

    /**
     * 把specList中的数据，转换为map集合数据
     *[
     *         "{'颜色': '黑色'}",
     *         "{'颜色': '红色'}",
     *         "{'颜色': '黑色', '尺码': '38'}",
     *         "{'颜色': '黑色', '尺码': '37'}",
     *         "{'颜色': '黑色', '尺码': '39'}",
     *         "{'颜色': '黑色', '尺码': '36'}",
     *         "{'颜色': '蓝色'}",
     *         "{'颜色': '黑色', '尺码': '35'}",
     *         "{'颜色': '蓝色', '尺码': '27'}"
     *     ]
     *
     *     [
     *      '颜色':"{'黑色','蓝色'}",
     *     '尺码':
     *     ]
     *
     */
    public Map<String, Set<String>> formartSpec(List<String> specList){
        Map<String, Set<String>> resultMap=new HashMap<>();
        if (specList!=null && specList.size()>0){
            for (String specJsonString : specList) {
                //将json数据转换为map
                Map<String,String> specMap = JSON.parseObject(specJsonString, Map.class);
                for (String specKey : specMap.keySet()) {
                    Set<String> specSet = resultMap.get(specKey);//获取map中的规格set，如果没有就新建一个
                    if (specSet==null){
                        specSet = new HashSet<String>();
                    }
                    //将规格的值放入set
                    specSet.add(specMap.get(specKey));
                    //将set放入map中
                    resultMap.put(specKey,specSet);
                }

            }
        }



        return resultMap;
    }
}
