package com.pinyougou.sellergoods.service;

import java.util.List;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

/**
 * 品牌服务接口
 * 
 * @author fushanlin
 *
 */
public interface BrandService {
	
	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbBrand> findAll();
	
	public PageResult findPage(int pageNum,int pageSize);
	
	/**
	 * 增加
	*/
	public void add(TbBrand brand);
	
	/**
	 * 修改
	 */
	public void update(TbBrand brand);
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbBrand findOne(Long id);
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long [] ids);
	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbBrand brand, int pageNum,int pageSize);
}
