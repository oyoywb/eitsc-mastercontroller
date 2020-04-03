package com.allimu.mastercontroller.service;



import com.allimu.mastercontroller.netty.model.Message;

import io.netty.channel.ChannelHandlerContext;



public interface DeviceService {
	public void saveDeviceBindInfo(Message message ,ChannelHandlerContext ctx);
	public void saveEnvironmentalData(Message message);	
	public void saveElectricityConsumption(Message message);
	public void saveDeviceState(Message message);
	public void exceptionHandling(String sn);
	
	
}
