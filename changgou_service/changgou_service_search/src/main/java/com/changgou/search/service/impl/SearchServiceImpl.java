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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * //按照查询条件进行数据查询
     * @param searchMap
     * @return
     * @throws Exception
     */
    @Override
    public Map search(Map<String, String> searchMap) throws Exception {

        HashMap<String, Object> resultMap = new HashMap<>();

        //构建查询
        if (searchMap!=null){
            //构建查询条件封装对象
            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            //按照关键字查询,对条件的封装
            if (StringUtils.isNotEmpty(searchMap.get("keywords"))){
                //matchQuery是模糊查询
                boolQuery.must(QueryBuilders.matchQuery("name",searchMap.get("keywords")).operator(Operator.AND));
            }

            //按照品牌进行过滤查询
            if (StringUtils.isNotEmpty(searchMap.get("brand"))){
                //termQuery是完全匹配查询，精确查询
                //brandName 是ES的索引库中的一个域名
                boolQuery.filter(QueryBuilders.termQuery("brandName",searchMap.get("brand")));
            }

            //按照规格进行过滤查询
            for (String key: searchMap.keySet()){
                if (key.startsWith("spec_")){

                    String value=searchMap.get(key).replace("%2B","+");
                    //key:spec_网络制式  需要裁剪
                    //域名是  specMap.网络制式.keyword
                    boolQuery.filter(QueryBuilders.termQuery(("specMap."+key.substring(5)+".keyword"),value));
                }

            }

            //按照价格进行区间过滤查询
            if (StringUtils.isNotEmpty(searchMap.get("price"))){
                String[] prices = searchMap.get("price").split("-");
                if (prices.length==2){
                    //价格是1000-2000  0-500  .lte是小于等于
                    boolQuery.filter(QueryBuilders.rangeQuery("price").lte(prices[1]));

                }
                //  .gte是大于等于
                boolQuery.filter(QueryBuilders.rangeQuery("price").gte(prices[0]));

            }



            nativeSearchQueryBuilder.withQuery(boolQuery);

            //按照品牌进行分组（聚合）查询
            String skuBrand="skuBrand";
            //skuBrand 是聚合结果的列名
            //field 是对哪一个域进行分组查询
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuBrand).field("brandName"));


            //按照规格进行聚合查询
            String skuSpec="skuSpec";
            //terms(skuSpec) 是进行分组查询后分组的字段列
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuSpec).field("spec.keyword"));





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
                    if (hits!=null){
                        //有查询结果  SearchHit就是索引库中查到的每一条记录，每一个document
                        for (SearchHit hit : hits) {
                            //searchHit对象转为skuinfo对象
                            SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);
                            list.add((T) skuInfo);

                        }

                    }
                    return new AggregatedPageImpl<T>(list,pageable,hits.getTotalHits(),searchResponse.getAggregations());
                }
            });

            //封装查询结果
            //总记录数
            resultMap.put("total",resultInfo.getTotalElements());
            //总页数
            resultMap.put("totalPages",resultInfo.getTotalPages());
            //数据集合
            resultMap.put("rows",resultInfo.getContent());

            //封装品牌的分组结果
            StringTerms brandTerms = (StringTerms) resultInfo.getAggregation(skuBrand);
            //流运算
            List<String> brandList = brandTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            resultMap.put("brandList",brandList);


            //封装规格的分组结果
            StringTerms specTerms = (StringTerms) resultInfo.getAggregation(skuSpec);
            List<String> specList = specTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            resultMap.put("specList",specList);


            return resultMap;

        }




        return null;
    }
}
