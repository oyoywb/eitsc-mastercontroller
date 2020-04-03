package com.allimu.mastercontroller.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.allimu.mastercontroller.dao.CodeReflectDao;
import com.allimu.mastercontroller.dao.DeviceBindDetailInfoDao;
import com.allimu.mastercontroller.dao.DeviceBindInfoDao;
import com.allimu.mastercontroller.dao.DeviceStateDao;
import com.allimu.mastercontroller.dao.ElectricityConsumptionDao;
import com.allimu.mastercontroller.dao.EnvironmentalDataDao;
import com.allimu.mastercontroller.dao.EquipDao;
import com.allimu.mastercontroller.netty.code.DateInter;
import com.allimu.mastercontroller.netty.code.EnvironmentUnit;
import com.allimu.mastercontroller.netty.model.DeviceBindDetailInfo;
import com.allimu.mastercontroller.netty.model.DeviceBindInfo;
import com.allimu.mastercontroller.netty.model.DeviceState;
import com.allimu.mastercontroller.netty.model.ElectricityConsumption;
import com.allimu.mastercontroller.netty.model.EnvironmentalData;
import com.allimu.mastercontroller.netty.model.Equip;
import com.allimu.mastercontroller.netty.model.Message;
import com.allimu.mastercontroller.netty.model.SchoolCode;
import com.allimu.mastercontroller.remote.service.InstructionCodeRemoteService;
import com.allimu.mastercontroller.service.DeviceService;

import io.netty.channel.ChannelHandlerContext;

@Service
public class DeviceServiceImpl implements DeviceService {

	@Autowired
	private CodeReflectDao codeReflectDao;
	@Autowired
	private DeviceBindInfoDao deviceBindInfoDao;
	@Autowired
	private DeviceBindDetailInfoDao deviceBindDetailInfoDao;
	@Autowired
	private EnvironmentalDataDao environmentalDataDao;
	@Autowired
	private ElectricityConsumptionDao electricityConsumptionDao;
	@Autowired
	private DeviceStateDao deviceStateDao;
	@Autowired
	private EquipDao equipDao;
	@Autowired
	InstructionCodeRemoteService instructionCodeRemoteService;

	/**
	 * 保存设备连接信息与设备绑定信息
	 */
	@Transactional
	@Override
	public void saveDeviceBindInfo(Message message, ChannelHandlerContext ctx) {		
		Equip snEquip = equipDao.getSnEquipBySn(message.getSn());
		DeviceBindDetailInfo deviceBindDetailInfo = (DeviceBindDetailInfo) message.getObject();
		deviceBindDetailInfo.setSchoolCode(snEquip.getSchoolCode());
		deviceBindDetailInfo.setSchoolName(snEquip.getSchoolName());
		deviceBindDetailInfo.setBuildCode(snEquip.getBuildCode());
		deviceBindDetailInfo.setBuildName(snEquip.getBuildName());
		deviceBindDetailInfo.setClassRoomCode(snEquip.getClassCode());
		deviceBindDetailInfo.setClassRoomName(snEquip.getClassName());
		System.out.println(deviceBindDetailInfo);
		// 如果不是红外设备
		if (deviceBindDetailInfo.getDevice() != (short) 0x0006) {
			// 如果本地数据库中未保存该条设备连接信息，则添加设备后保存
			if (deviceBindDetailInfoDao.getDeviceBindDetailInfoByIeee(deviceBindDetailInfo.getIeee(),
					deviceBindDetailInfo.getEndpoint())==null) {
				deviceBindDetailInfoDao.saveDeviceBindDetailInfo(deviceBindDetailInfo);
				System.out.println("保存设备连接信息成功 ------->" + deviceBindDetailInfo);
				DeviceBindInfo deviceBindInfo = new DeviceBindInfo();
				// 根据设备连接信息生成临时绑定信息
				deviceBindInfo.setTempId(deviceBindDetailInfo.getId());
				deviceBindInfo.setSchoolCode(deviceBindDetailInfo.getSchoolCode());
				deviceBindInfo.setSchoolName(deviceBindDetailInfo.getSchoolName());
				deviceBindInfo.setBuildCode(deviceBindDetailInfo.getBuildCode());
				deviceBindInfo.setBuildName(deviceBindDetailInfo.getBuildName());
				deviceBindInfo.setClassRoomCode(deviceBindDetailInfo.getClassRoomCode());
				deviceBindInfo.setClassRoomName(deviceBindDetailInfo.getClassRoomName());
				deviceBindInfo.setEquipmentType(codeReflectDao.getEquipmentType(deviceBindDetailInfo.getDevice()));
				deviceBindInfoDao.saveDeviceBindInfo(deviceBindInfo);
				System.out.println("保存临时绑定表成功------->   " + deviceBindInfo);
			} else { // 如果本地以保存设备连接信息则更新
				// 如果断开重连，也会上传临时绑定信息，这时候要将设备状态从离线转变为开。或者关
				// 如果还未绑定设备编号则丢弃该信息，反之保存
				if (deviceBindDetailInfo.getEquipmentCode() != null) {
					DeviceState deviceState = new DeviceState();
					deviceState.setSchoolCode(deviceBindDetailInfo.getSchoolCode());
					deviceState.setSchoolName(deviceBindDetailInfo.getSchoolName());
					deviceState.setBuildCode(deviceBindDetailInfo.getBuildCode());
					deviceState.setBuildName(deviceBindDetailInfo.getBuildName());
					deviceState.setClassRoomCode(deviceBindDetailInfo.getClassRoomCode());
					deviceState.setClassRoomName(deviceBindDetailInfo.getClassRoomName());
					deviceState.setEquipmentCode(deviceBindDetailInfo.getEquipmentCode());
					deviceState.setEquipmentName(deviceBindDetailInfo.getEquipmentName());
					deviceState.setState(deviceBindDetailInfo.getState());
					deviceStateDao.saveDeviceState(deviceState);
					System.out.println("将设备信息状态重离线改成在线------->" + deviceState);
				}
				deviceBindDetailInfoDao.updateDeviceBindDetailInfo(deviceBindDetailInfo);
			}
		} else { // 如果是红外设备
			int readDeviceCount = instructionCodeRemoteService.getReadDeviceCount(deviceBindDetailInfo.getSchoolCode(),
					deviceBindDetailInfo.getBuildCode(), deviceBindDetailInfo.getClassRoomCode());
			List<DeviceBindDetailInfo> readDeviceList = deviceBindDetailInfoDao
					.getDeviceBindDetailInfoListByIeee(deviceBindDetailInfo.getIeee(), deviceBindDetailInfo.getEndpoint());
			// 先判断是否相等，如果不相等，可能是新增了设备
			if (readDeviceCount != readDeviceList.size()) {
				for (int i = 0; i < readDeviceCount - readDeviceList.size(); i++) {
					DeviceBindDetailInfo dbdi = deviceBindDetailInfo.clone();
					deviceBindDetailInfoDao.saveDeviceBindDetailInfo(dbdi);
					System.out.println("保存设备连接信息成功 ------->" + deviceBindDetailInfo);
					DeviceBindInfo deviceBindInfo = new DeviceBindInfo();
					// 根据设备连接信息生成临时绑定信息
					deviceBindInfo.setTempId(dbdi.getId());
					deviceBindInfo.setSchoolCode(dbdi.getSchoolCode());
					deviceBindInfo.setSchoolName(dbdi.getSchoolName());
					deviceBindInfo.setBuildCode(dbdi.getBuildCode());
					deviceBindInfo.setBuildName(dbdi.getBuildName());
					deviceBindInfo.setClassRoomCode(dbdi.getClassRoomCode());
					deviceBindInfo.setClassRoomName(dbdi.getClassRoomName());
					deviceBindInfo.setEquipmentType(codeReflectDao.getEquipmentType(dbdi.getDevice()));
					deviceBindInfoDao.saveDeviceBindInfo(deviceBindInfo);
					System.out.println("保存临时绑定表成功------->" + deviceBindInfo);
				}
			} else { // 如果相等，是发生了断网重连
				for (DeviceBindDetailInfo readDevice : readDeviceList) {
					if (deviceBindDetailInfo.getEquipmentCode() != null) {
						DeviceState deviceState = new DeviceState();
						deviceState.setSchoolCode(readDevice.getSchoolCode());
						deviceState.setSchoolName(readDevice.getSchoolName());
						deviceState.setBuildCode(readDevice.getBuildCode());
						deviceState.setBuildName(readDevice.getBuildName());
						deviceState.setClassRoomCode(readDevice.getClassRoomCode());
						deviceState.setClassRoomName(readDevice.getClassRoomName());
						deviceState.setEquipmentCode(readDevice.getEquipmentCode());
						deviceState.setEquipmentName(readDevice.getEquipmentName());
						deviceState.setState(readDevice.getState());
						deviceStateDao.saveDeviceState(deviceState);
						System.out.println("将设备信息状态重离线改成在线------->" + deviceState);
					}
				}
			}
		}
	}

	/**
	 * 保存环境数据
	 */
	@Transactional
	@Override
	public void saveEnvironmentalData(Message message) {
		if ((new Date().getTime() - DateInter.getDate().getTime()) <= 10000l) {
			DeviceBindDetailInfo deviceBindDetailInfo = deviceBindDetailInfoDao.getDeviceBindDetailInfoByFactorys(message.getSn(),
					message.getAddress(), message.getEndpoint());
			// 如果设备连接信息不为空则保存数据，反之不保存
			if (deviceBindDetailInfo != null) {
				EnvironmentalData environmentalData = new EnvironmentalData();
				environmentalData.setSchoolCode(deviceBindDetailInfo.getSchoolCode());
				environmentalData.setSchoolName(deviceBindDetailInfo.getSchoolName());
				environmentalData.setBuildCode(deviceBindDetailInfo.getBuildCode());
				environmentalData.setBuildName(deviceBindDetailInfo.getBuildName());
				environmentalData.setClassRoomCode(deviceBindDetailInfo.getClassRoomCode());
				environmentalData.setClassRoomName(deviceBindDetailInfo.getClassRoomName());
				environmentalData.setType(codeReflectDao.getEnviromentType(message.getClusterId()));
				environmentalData.setUnit(EnvironmentUnit.getUnit(environmentalData.getType()));
				if (message.getValue() != null)
					environmentalData.setValue(message.getValue());
				environmentalData.setSchoolCode(SchoolCode.schoolCode);
				environmentalData.setIsUpload(false);
				environmentalDataDao.saveEnvironmentalData(environmentalData);
				System.out.println("接收到环境数据--->  " + environmentalData);
			} else {
				System.out.println(message);
				System.out.println("该设备未绑定，数据抛弃");
			}
		}
	}

	/**
	 * 保存用电量
	 */
	@Transactional
	@Override
	public void saveElectricityConsumption(Message message) {
		DeviceBindDetailInfo deviceBindDetailInfo = deviceBindDetailInfoDao.getDeviceBindDetailInfoByFactorys(message.getSn(),
				message.getAddress(), message.getEndpoint());
		// 如果设备连接信息不为空则保存数据，反之不保存
		if (deviceBindDetailInfo != null) {
			ElectricityConsumption electricityConsumption = new ElectricityConsumption();
			electricityConsumption.setSchoolCode(deviceBindDetailInfo.getSchoolCode());
			electricityConsumption.setSchoolName(deviceBindDetailInfo.getSchoolName());
			electricityConsumption.setBuildCode(deviceBindDetailInfo.getBuildCode());
			electricityConsumption.setBuildName(deviceBindDetailInfo.getBuildName());
			electricityConsumption.setClassRoomCode(deviceBindDetailInfo.getClassRoomCode());
			electricityConsumption.setClassRoomName(deviceBindDetailInfo.getClassRoomName());
			electricityConsumption.setEquipmentCode(deviceBindDetailInfo.getEquipmentCode());
			electricityConsumption.setEquipmentName(deviceBindDetailInfo.getEquipmentName());
			electricityConsumption.setIsUpload(false);
			electricityConsumption.setStartTime(new Date());
			electricityConsumption.setState(message.getValue());
			electricityConsumptionDao.saveElectricityConsumption(electricityConsumption);
			System.out.println("接收到用电量信息----->  " + electricityConsumption);
		} else {
			System.out.println("该设备未绑定，数据抛弃");
		}
	}

	/**
	 * 保存设备状态
	 */
	@Transactional
	@Override
	public void saveDeviceState(Message message) {
		DeviceBindDetailInfo deviceBindDetailInfo = deviceBindDetailInfoDao.getDeviceBindDetailInfoByFactorys(message.getSn(),
				message.getAddress(), message.getEndpoint());
		System.out.println(message);
		// 如果设备连接信息不为空则保存数据，反之不保存
		if (deviceBindDetailInfo != null) {
			DeviceState deviceState = new DeviceState();
			deviceState.setSchoolCode(deviceBindDetailInfo.getSchoolCode());
			deviceState.setSchoolName(deviceBindDetailInfo.getSchoolName());
			deviceState.setBuildCode(deviceBindDetailInfo.getBuildCode());
			deviceState.setBuildName(deviceBindDetailInfo.getBuildName());
			deviceState.setClassRoomCode(deviceBindDetailInfo.getClassRoomCode());
			deviceState.setClassRoomName(deviceBindDetailInfo.getClassRoomName());
			deviceState.setEquipmentCode(deviceBindDetailInfo.getEquipmentCode());
			deviceState.setEquipmentName(deviceBindDetailInfo.getEquipmentName());
			deviceState.setState(message.getState());
			deviceState.setIsUpload(false);
			deviceStateDao.saveDeviceState(deviceState);
			System.out.println("接收到设备状态数据 ----->  " + deviceState);
		} else {
			System.out.println("该设备未绑定，数据抛弃");
		}
	}

	/**
	 * 异常处理，如果网关断线，设置全部网关底下的设备为离线状态
	 */
	public void exceptionHandling(String sn) {
		List<DeviceBindDetailInfo> deviceBindDetailInfoList = deviceBindDetailInfoDao.getDeviceBindDetailInfoBySn(sn);
		if (deviceBindDetailInfoList != null) {
			List<DeviceState> deviceStateList = new ArrayList<DeviceState>();
			for (DeviceBindDetailInfo deviceBindDetailInfo : deviceBindDetailInfoList) {
				if (deviceBindDetailInfo.getEquipmentCode() != null) {
					DeviceState deviceState = new DeviceState();
					deviceState.setSchoolCode(deviceBindDetailInfo.getSchoolCode());
					deviceState.setSchoolName(deviceBindDetailInfo.getSchoolName());
					deviceState.setBuildCode(deviceBindDetailInfo.getBuildCode());
					deviceState.setBuildName(deviceBindDetailInfo.getBuildName());
					deviceState.setClassRoomCode(deviceBindDetailInfo.getClassRoomCode());
					deviceState.setClassRoomName(deviceBindDetailInfo.getClassRoomName());
					deviceState.setEquipmentCode(deviceBindDetailInfo.getEquipmentCode());
					deviceState.setEquipmentName(deviceBindDetailInfo.getEquipmentName());
					deviceState.setState((byte) 2);
					deviceStateList.add(deviceState);
				}
			}
			deviceStateDao.saveDeviceStateList(deviceStateList);
		}
	}

}
