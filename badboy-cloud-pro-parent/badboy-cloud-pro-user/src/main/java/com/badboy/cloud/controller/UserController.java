package com.badboy.cloud.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.badboy.cloud.domain.User;
import com.badboy.cloud.feign.order.OrderFeign;
import com.badboy.cloud.feign.shop.ShopFeign;
import com.badboy.cloud.mq.rabbitmq.user.producer.UserLoginDateProducer;
import com.badboy.cloud.service.IUserService;

@RestController
public class UserController {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource 
	private IUserService userService;
	
	@Autowired
	private ShopFeign shopFeigen;
	@Autowired
	private OrderFeign orderFeigen;
	
	@Autowired
	private UserLoginDateProducer UserLoginDateProducer;
	
	@Value("${badboy-user-config}")
	private String badboyConfigKey;
	
	@GetMapping(value="/getInfo/{uuid}")
	public User index(HttpServletRequest request,
					@PathVariable String uuid) {
		
		logger.info("Say:{}","/getInfo/{uuid}");
		logger.info("<call 2223 TraceId={},SpanId={}>",request.getHeader("X-B3-TraceId"),request.getHeader("X-B3-SpanId"));
		
		User u = userService.getUserInfo(uuid);
		u.setListOrder(orderFeigen.getListOrder(uuid));
		u.setListShop(shopFeigen.getListOrder(uuid));
		
		//异步处理消息
		UserLoginDateProducer.userLoginDate(u);
		
		return u;
	}
	
	@GetMapping(value="/key")
	public String getConfigKey() {
		return badboyConfigKey;
	}
}
