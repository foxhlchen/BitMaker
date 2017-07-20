package com.okcoin.websocket.test;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.okcoin.websocket.WebSocketBase;
import com.okcoin.websocket.WebSocketService;
/**
 * 订阅信息处理类需要实现WebSocketService接口
 * @author okcoin
 *
 */
public class BuissnesWebSocketServiceImpl implements WebSocketService{
	private Logger log = LogManager.getLogger(WebSocketBase.class);
	@Override
	public void onReceive(String msg){
		
		log.info("WebSocket Client received message: " + msg);
	
	}
}
