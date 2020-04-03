package com.allimu.mastercontroller.remote.service;

import java.util.List;


import com.allimu.mastercontroller.netty.model.DeviceBindInfo;
import com.allimu.mastercontroller.netty.model.DeviceState;
import com.allimu.mastercontroller.netty.model.ElectricityConsumption;
import com.allimu.mastercontroller.netty.model.EnvironmentalData;
import com.allimu.mastercontroller.netty.model.JkCode;
import com.allimu.mastercontroller.netty.model.JkTestCode;
import com.allimu.mastercontroller.netty.model.TempReflect;


/**
  * 供网关调用远程接口
 * @author ymsn
 * @date  2020年1月7日
 */
public interface InstructionCodeRemoteService {
	//根据学校编号获取指令
	List<JkCode> getJkCodeBySchoolCode(Long schoolCode);
	//指令发送给网关后，设置该指令为已调用
	int updateJkCode(JkCode jkCode);	
	
	//根据学校编号获取测试指令
	List<JkTestCode> getJkTestCodeBySchoolCode(Long schoolCode);
	//指令发送给网关后，设置该指令为已调用
	int updateJkTestCode(JkTestCode jkTestCode);
		
	//保存设备与网关的绑定信息到云端
	int saveDeviceBindInfo(List<DeviceBindInfo> deviceBindInfoList);
	//保存设备状态信息
	int saveDeviceState(List<DeviceState> deviceStateList);
	//保存电箱耗电量
	int saveElectricityConsumption(List<ElectricityConsumption> electricityConsumption);	
	//保存环境数据
	int saveEnvironmentalData (List<EnvironmentalData> environmentalData);
	
	//获取临时id与设备编号的绑定
	List<TempReflect> getTempReflect(Long schoolCode);
	
	String getWgEquipBySchoolCode(Long schoolCode);
	
	int updateTempReflect(TempReflect tempReflect);
	
	int getReadDeviceCount(Long schoolCode, Long buildCode, Long classRoomCode);
	
	String getAirEquipByEquipmentCode(Long schoolCode,String equipmentCode);
	
}
