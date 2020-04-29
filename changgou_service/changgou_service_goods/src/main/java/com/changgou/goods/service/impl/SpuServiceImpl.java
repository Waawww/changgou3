package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.dao.*;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SpuService;
import com.changgou.order.pojo.OrderItem;
import com.changgou.util.IdWorker;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;


    /**
     * 查询全部列表
     *
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    @Override
    public Spu findById(String id) {
        return spuMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     *
     * @param goods
     */
    @Override
    @Transactional
    public void add(Goods goods) {
        //1.添加sup
        Spu spu = goods.getSpu();
        //1.1设置分布式id
        long spuId = idWorker.nextId();
        spu.setId(String.valueOf(spuId));
        //1.2设置删除状态  0：不删除
        spu.setIsDelete("0");
        //1.3设置上架状态 0:未上架  1：上架
        spu.setIsMarketable("0");
        //1.4设置审状态 0:未审核  1：已审核
        spu.setStatus("0");
        spuMapper.insertSelective(spu);//添加到数据库

        //2.添加sku集合
        this.saveSkuList(goods);

    }

    //添加sku集合的方法
    private void saveSkuList(Goods goods) {
        Spu spu = goods.getSpu();

        //查询分类对象
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());

        //查询品牌对象
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());

        //设置品牌与分类的关联关系
        //查询关联表
        CategoryBrand categoryBrand = new CategoryBrand();
        categoryBrand.setBrand_id(spu.getBrandId());
        categoryBrand.setCategory_id(spu.getCategory3Id());
        int count = categoryBrandMapper.selectCount(categoryBrand);
        if (count == 0) {
            //品牌和分类没有关联关系
            categoryBrandMapper.insert(categoryBrand);
        }

        //获取sku集合
        List<Sku> skuList = goods.getSkuList();
        if (skuList != null) {
            //遍历集合，循环填充数据，并添加到数据库
            for (Sku sku : skuList) {
                //设置id
                long skuId = idWorker.nextId();
                sku.setId(String.valueOf(skuId));
                //设置sku规格数据
                if (StringUtils.isEmpty(sku.getSpec())) {
                    sku.setSpec("{}");
                }
                //设置sku名称（spu名称+规格）
                //spu名称
                String name = spu.getName();
                //将规格json转为map,将map 中的value进行名称的拼接
                Map<String, String> specMap = JSON.parseObject(sku.getSpec(), Map.class);
                if (specMap != null && specMap.size() > 0) {
                    for (String value : specMap.values()) {
                        name += " " + value;
                    }
                }
                sku.setName(name);

                //设置spuid
                sku.setSpuId(spu.getId());
                //设置创建与修改时间
                sku.setCreateTime(new Date());
                sku.setUpdateTime(new Date());
                //设置商品分类id
                sku.setCategoryId(category.getId());
                //设置商品分类名称
                sku.setCategoryName(category.getName());
                //设置品牌名称
                sku.setBrandName(brand.getName());
                //将sku添加到数据库
                skuMapper.insertSelective(sku);

            }
        }

    }


    /**
     * 修改
     *
     * @param goods
     */
    @Override
    @Transactional
    public void update(Goods goods) {
        //修改spu
        Spu spu = goods.getSpu();
        spuMapper.updateByPrimaryKey(spu);

        //修改sku，
        //删除原有sku,添加新的sku
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", spu.getId());
        skuMapper.deleteByExample(example);
        //添加新的sku
        this.saveSkuList(goods);

    }

    /**
     * 删除,逻辑删除，is_delete=1
     *
     * @param id
     */
    @Override
    @Transactional
    public void delete(String id) {
        //逻辑删除
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("当前商品不存在");
        }
        //判断商品是否为下架状态
        if (!spu.getIsMarketable().equals("0")) {
            throw new RuntimeException("当前商品未下架，无法删除");
        }
        //如果是下架，则修改标记位
        spu.setIsDelete("1");
        spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }


    /**
     * 条件查询
     *
     * @param searchMap
     * @return
     */
    @Override
    public List<Spu> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Spu> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        return (Page<Spu>) spuMapper.selectAll();
    }

    /**
     * 条件+分页查询
     *
     * @param searchMap 查询条件
     * @param page      页码
     * @param size      页大小
     * @return 分页结果
     */
    @Override
    public Page<Spu> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        return (Page<Spu>) spuMapper.selectByExample(example);
    }

    /**
     * 根据id值查询SPU和SKU列表
     *
     * @param id
     * @return
     */
    @Override
    public Goods findGoodsById(String id) {
        Goods goods = new Goods();

        //1.查询spu,封装到goods
        Spu spu = spuMapper.selectByPrimaryKey(id);
        goods.setSpu(spu);

        //2.查询sku集合，封装到goods
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        //根据spu进行sku列表的查询
        criteria.andEqualTo("spuId", id);
        List<Sku> skuList = skuMapper.selectByExample(example);
        goods.setSkuList(skuList);

        return goods;
    }

    /**
     * 审核商品,并自动上架
     *
     * @param id
     */
    @Override
    @Transactional
    public void audit(String id) {
        //查询spu对象
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("当前商品不存在");
        }
        //判断当前spu是否处于删除状态
        if ("1".equals(spu.getIsDelete())) {
            throw new RuntimeException("当前商品处于删除状态");
        }
        //不处于删除状态,修改审核状态为1,上下架状态为1
        spu.setStatus("1");
        spu.setIsMarketable("1");
        //执行修改操作
        spuMapper.updateByPrimaryKeySelective(spu);

    }

    /**
     * 下架商品
     *
     * @param id
     */
    @Override
    @Transactional
    public void pull(String id) {
        //查询spu对象
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("当前商品不存在");
        }
        //判断当前spu是否处于删除状态
        if ("1".equals(spu.getIsDelete())) {
            throw new RuntimeException("当前商品处于删除状态");
        }
        //不处于删除状态,修改上下架状态为0
        spu.setIsMarketable("0");

        //执行修改操作
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 上架商品
     *
     * @param id
     */
    @Override
    @Transactional
    public void put(String id) {
        //查询spu对象
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("当前商品不存在");
        }
        //判断当前spu是否处于删除状态
        if ("1".equals(spu.getIsDelete())) {
            throw new RuntimeException("当前商品处于删除状态");
        }
        //判断当前spu是否处于审核状态
        if (!"1".equals(spu.getStatus())) {
            throw new RuntimeException("当前商品处于未审核状态");
        }
        //不处于删除状态,修改上下架状态为1
        spu.setIsMarketable("1");

        //执行修改操作
        spuMapper.updateByPrimaryKeySelective(spu);

    }


    /**
     * 还原被逻辑删除的商品
     *
     * @param id
     */
    @Override
    @Transactional
    public void restore(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("当前商品不存在");
        }
        //判断商品是否为已删除
        if (!spu.getIsDelete().equals("1")) {
            throw new RuntimeException("商品未被删除");
        }
        spu.setIsDelete("0");
        spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 物理删除
     * @param id
     */
    @Override
    @Transactional
    public void realDel(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("当前商品不存在");
        }
        //判断商品是否是已经删除状态
        if (!spu.getIsDelete().equals("1")){
            throw new RuntimeException("此商品未被删除");
        }
        //执行删除操作
        spuMapper.deleteByPrimaryKey(id);
    }




    /**
     * 构建查询对象
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 主键
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                criteria.andEqualTo("id", searchMap.get("id"));
            }
            // 货号
            if (searchMap.get("sn") != null && !"".equals(searchMap.get("sn"))) {
                criteria.andEqualTo("sn", searchMap.get("sn"));
            }
            // SPU名
            if (searchMap.get("name") != null && !"".equals(searchMap.get("name"))) {
                criteria.andLike("name", "%" + searchMap.get("name") + "%");
            }
            // 副标题
            if (searchMap.get("caption") != null && !"".equals(searchMap.get("caption"))) {
                criteria.andLike("caption", "%" + searchMap.get("caption") + "%");
            }
            // 图片
            if (searchMap.get("image") != null && !"".equals(searchMap.get("image"))) {
                criteria.andLike("image", "%" + searchMap.get("image") + "%");
            }
            // 图片列表
            if (searchMap.get("images") != null && !"".equals(searchMap.get("images"))) {
                criteria.andLike("images", "%" + searchMap.get("images") + "%");
            }
            // 售后服务
            if (searchMap.get("saleService") != null && !"".equals(searchMap.get("saleService"))) {
                criteria.andLike("saleService", "%" + searchMap.get("saleService") + "%");
            }
            // 介绍
            if (searchMap.get("introduction") != null && !"".equals(searchMap.get("introduction"))) {
                criteria.andLike("introduction", "%" + searchMap.get("introduction") + "%");
            }
            // 规格列表
            if (searchMap.get("specItems") != null && !"".equals(searchMap.get("specItems"))) {
                criteria.andLike("specItems", "%" + searchMap.get("specItems") + "%");
            }
            // 参数列表
            if (searchMap.get("paraItems") != null && !"".equals(searchMap.get("paraItems"))) {
                criteria.andLike("paraItems", "%" + searchMap.get("paraItems") + "%");
            }
            // 是否上架
            if (searchMap.get("isMarketable") != null && !"".equals(searchMap.get("isMarketable"))) {
                criteria.andEqualTo("isMarketable", searchMap.get("isMarketable"));
            }
            // 是否启用规格
            if (searchMap.get("isEnableSpec") != null && !"".equals(searchMap.get("isEnableSpec"))) {
                criteria.andEqualTo("isEnableSpec", searchMap.get("isEnableSpec"));
            }
            // 是否删除
            if (searchMap.get("isDelete") != null && !"".equals(searchMap.get("isDelete"))) {
                criteria.andEqualTo("isDelete", searchMap.get("isDelete"));
            }
            // 审核状态
            if (searchMap.get("status") != null && !"".equals(searchMap.get("status"))) {
                criteria.andEqualTo("status", searchMap.get("status"));
            }

            // 品牌ID
            if (searchMap.get("brandId") != null) {
                criteria.andEqualTo("brandId", searchMap.get("brandId"));
            }
            // 一级分类
            if (searchMap.get("category1Id") != null) {
                criteria.andEqualTo("category1Id", searchMap.get("category1Id"));
            }
            // 二级分类
            if (searchMap.get("category2Id") != null) {
                criteria.andEqualTo("category2Id", searchMap.get("category2Id"));
            }
            // 三级分类
            if (searchMap.get("category3Id") != null) {
                criteria.andEqualTo("category3Id", searchMap.get("category3Id"));
            }
            // 模板ID
            if (searchMap.get("templateId") != null) {
                criteria.andEqualTo("templateId", searchMap.get("templateId"));
            }
            // 运费模板id
            if (searchMap.get("freightId") != null) {
                criteria.andEqualTo("freightId", searchMap.get("freightId"));
            }
            // 销量
            if (searchMap.get("saleNum") != null) {
                criteria.andEqualTo("saleNum", searchMap.get("saleNum"));
            }
            // 评论数
            if (searchMap.get("commentNum") != null) {
                criteria.andEqualTo("commentNum", searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
