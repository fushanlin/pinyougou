package com.pinyougou.search.service.impl;

import java.util.Arrays;

import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.activemq.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.search.service.ItemSearchService;

/**
 * 监听：用于删除索引库中记录
 * @author Administrator
 *
 */
@Component
public class ItemDeleteListener implements MessageListener{

	@Autowired
	private ItemSearchService itemSearchService;
	

	@Override
	public void onMessage(javax.jms.Message message) {
		try {			
			ObjectMessage objectMessage= (ObjectMessage)message;
			Long[]  goodsIds = (Long[]) objectMessage.getObject();		
			System.out.println("ItemDeleteListener监听接收到消息..."+goodsIds);
			itemSearchService.deleteByGoodsIds(Arrays.asList(goodsIds));
			System.out.println("成功删除索引库中的记录");			
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

}