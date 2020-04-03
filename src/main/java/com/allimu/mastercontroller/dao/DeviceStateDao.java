package com.allimu.mastercontroller.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.allimu.mastercontroller.netty.model.DeviceState;

public interface DeviceStateDao {
	// 保存设备状态信息到本地数据库
	public int saveDeviceState(DeviceState deviceState);

	// 获取未上传到云端的设备状态信息
	public List<DeviceState> getDeviceState();

	// 更新设备状态信息，设置为已上传
	public int updateDeviceStateList(@Param("deviceStateList") List<DeviceState> deviceStateList);

	// 批量保存设备状态信息
	public int saveDeviceStateList(@Param("deviceStateList")List<DeviceState> deviceStateList);
}
