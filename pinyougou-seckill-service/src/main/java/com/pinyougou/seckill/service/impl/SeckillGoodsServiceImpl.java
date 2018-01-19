package com.pinyougou.seckill.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillGoodsService;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillGoods> findAll() {
		return seckillGoodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbSeckillGoods> page = (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillGoods seckillGoods) {
		seckillGoodsMapper.insert(seckillGoods);
	}

	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillGoods seckillGoods) {
		seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillGoods findOne(Long id) {
		return seckillGoodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			seckillGoodsMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbSeckillGoodsExample example = new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();

		if (seckillGoods != null) {
			if (seckillGoods.getTitle() != null && seckillGoods.getTitle().length() > 0) {
				criteria.andTitleLike("%" + seckillGoods.getTitle() + "%");
			}
			if (seckillGoods.getSmallPic() != null && seckillGoods.getSmallPic().length() > 0) {
				criteria.andSmallPicLike("%" + seckillGoods.getSmallPic() + "%");
			}
			if (seckillGoods.getSellerId() != null && seckillGoods.getSellerId().length() > 0) {
				criteria.andSellerIdLike("%" + seckillGoods.getSellerId() + "%");
			}
			if (seckillGoods.getStatus() != null && seckillGoods.getStatus().length() > 0) {
				criteria.andStatusLike("%" + seckillGoods.getStatus() + "%");
			}
			if (seckillGoods.getIntroduction() != null && seckillGoods.getIntroduction().length() > 0) {
				criteria.andIntroductionLike("%" + seckillGoods.getIntroduction() + "%");
			}

		}

		Page<TbSeckillGoods> page = (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/*
	 * @Override public List<TbSeckillGoods> findList() { TbSeckillGoodsExample
	 * example=new TbSeckillGoodsExample(); Criteria criteria =
	 * example.createCriteria(); criteria.andStatusEqualTo("1");//审核通过
	 * criteria.andStockCountGreaterThan(0);//剩余库存大于0
	 * criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
	 * criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间 return
	 * seckillGoodsMapper.selectByExample(example); }
	 */

	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public List<TbSeckillGoods> findList() {
		// 获取秒杀商品列表
		List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
		if (seckillGoodsList == null || seckillGoodsList.size() == 0) {
			TbSeckillGoodsExample example = new TbSeckillGoodsExample();
			Criteria criteria = example.createCriteria();
			criteria.andStatusEqualTo("1");// 审核通过
			criteria.andStockCountGreaterThan(0);// 剩余库存大于0
			criteria.andStartTimeLessThanOrEqualTo(new Date());// 开始时间小于等于当前时间
			criteria.andEndTimeGreaterThan(new Date());// 结束时间大于当前时间
			seckillGoodsList = seckillGoodsMapper.selectByExample(example);
			// 将商品列表装入缓存
			System.out.println("将秒杀商品列表装入缓存");
			for (TbSeckillGoods seckillGoods : seckillGoodsList) {
				redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
			}
		}
		return seckillGoodsList;
	}

	@Override
	public TbSeckillGoods findOneFromRedis(Long id) {
		return (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);
	}

	@Autowired
	private IdWorker idWorker;

	@Override
	public void submitOrder(Long seckillId, String userId) {
		// 从缓存中查询秒杀商品
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
		if (seckillGoods == null) {
			throw new RuntimeException("商品不存在");
		}
		if (seckillGoods.getStockCount() <= 0) {
			throw new RuntimeException("商品已抢购一空");
		}
		// 扣减（redis）库存
		seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
		redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);// 放回缓存
		if (seckillGoods.getStockCount() == 0) {// 如果已经被秒光
			seckillGoodsMapper.updateByPrimaryKey(seckillGoods);// 同步到数据库
			redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
		}
		// 保存（redis）订单
		long orderId = idWorker.nextId();
		TbSeckillOrder seckillOrder = new TbSeckillOrder();
		seckillOrder.setId(orderId);
		seckillOrder.setCreateTime(new Date());
		seckillOrder.setMoney(seckillGoods.getCostPrice());// 秒杀价格
		seckillOrder.setSeckillId(seckillId);
		seckillOrder.setSellerId(seckillGoods.getSellerId());
		seckillOrder.setUserId(userId);// 设置用户ID
		seckillOrder.setStatus("0");// 状态
		redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
	}
}
