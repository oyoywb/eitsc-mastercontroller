package com.allimu.mastercontroller.netty.config;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.allimu.mastercontroller.dao.CodeReflectDao;
import com.allimu.mastercontroller.dao.DeviceBindDetailInfoDao;
import com.allimu.mastercontroller.dao.DeviceBindInfoDao;
import com.allimu.mastercontroller.dao.DeviceStateDao;
import com.allimu.mastercontroller.dao.ElectricityConsumptionDao;
import com.allimu.mastercontroller.dao.EnvironmentalDataDao;
import com.allimu.mastercontroller.dao.EquipDao;
import com.allimu.mastercontroller.netty.code.DateInter;
import com.allimu.mastercontroller.netty.model.DeviceBindDetailInfo;
import com.allimu.mastercontroller.netty.model.DeviceBindInfo;
import com.allimu.mastercontroller.netty.model.DeviceState;
import com.allimu.mastercontroller.netty.model.ElectricityConsumption;
import com.allimu.mastercontroller.netty.model.EnvironmentalData;
import com.allimu.mastercontroller.netty.model.Equip;
import com.allimu.mastercontroller.netty.model.InstructionCode;
import com.allimu.mastercontroller.netty.model.JkCode;
import com.allimu.mastercontroller.netty.model.JkTestCode;
import com.allimu.mastercontroller.netty.model.SchoolCode;
import com.allimu.mastercontroller.netty.model.TempReflect;
import com.allimu.mastercontroller.remote.service.InstructionCodeRemoteService;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author Administrator 定时任务
 */
@Service
@EnableScheduling
public class CodeTask {
	@Autowired
	private InstructionCodeRemoteService remoteService;
	@Autowired
	private EquipDao equipDao;
	@Autowired
	private DeviceBindInfoDao deviceBindInfoDao;
	@Autowired
	private DeviceStateDao deviceStateDao;
	@Autowired
	private ElectricityConsumptionDao electricityConsumptionDao;
	@Autowired
	private EnvironmentalDataDao environmentalDataDao;
	@Autowired
	private DeviceBindDetailInfoDao deviceBindDetailInfoDao;
	@Autowired
	private CodeReflectDao codeReflectDao;

	private ChannelHandlerContext ctx1;

	private ChannelHandlerContext ctx2;

	private ChannelHandlerContext ctx3;

	/**
	 * * 定时任务获取网关设备
	 */
	@Scheduled(cron = "0/5 * * * * ?")
	public void getEquipBySchoolCode() {
		String str = remoteService.getWgEquipBySchoolCode(SchoolCode.schoolCode);
		JSONObject jsonObject = JSONObject.parseObject(str);
		String res = jsonObject.getString("data");
		if (res != null && !res.equals("[]")) {
			List<Equip> snEquipList = JSON.parseObject(res, new TypeReference<ArrayList<Equip>>() {
			});
			for (Equip snEquip : snEquipList) {
				if (equipDao.getSnEquipBySn(snEquip.getSn()) == null)
					equipDao.saveSnEquip(snEquip);
			}
		}
	}

	/**
	 * 定时任务上传状态数据，耗电量，设备状态，设备临时绑定信息到云端
	 */
	@Scheduled(cron = "*/3 * * * * ?")
	public void svaeToCloud() {
		int isUpload = 0;
		List<DeviceBindInfo> deviceBindInfoList = deviceBindInfoDao.getDeviceBindInfo();
		List<DeviceState> deviceStateList = deviceStateDao.getDeviceState();
		List<ElectricityConsumption> electricityConsumptionList = electricityConsumptionDao.getElectricityConsumption();
		List<EnvironmentalData> environmentalDataList = environmentalDataDao.getEnvironmentalData();
		if (deviceBindInfoList != null) {
			isUpload = remoteService.saveDeviceBindInfo(deviceBindInfoList);
			if (isUpload != 0)
				deviceBindInfoDao.updateDeviceBindInfoList(deviceBindInfoList);
		}
		if (deviceStateList != null) {
			isUpload = remoteService.saveDeviceState(deviceStateList);
			if (isUpload != 0)
				deviceStateDao.updateDeviceStateList(deviceStateList);
		}
		if (electricityConsumptionList != null) {
			isUpload = remoteService.saveElectricityConsumption(electricityConsumptionList);
			if (isUpload != 0)
				electricityConsumptionDao.updateElectricityConsumptionList(electricityConsumptionList);

		}
		if (environmentalDataList != null) {
			isUpload = remoteService.saveEnvironmentalData(environmentalDataList);
			if (isUpload != 0)
				environmentalDataDao.updateEnvironmentalDataList(environmentalDataList);
		}
	}

	/**
	 * 定时任务获取云端测试指令
	 */
	@Scheduled(cron = "*/2 * * * * ?")
	public void getTestCode() {
		List<JkTestCode> jkTestCodeList = remoteService.getJkTestCodeBySchoolCode(SchoolCode.schoolCode);
		if (jkTestCodeList != null) {
			List<InstructionCode> instructionCodeList = new ArrayList<InstructionCode>();
			jkTestCodeToInstructionCode(jkTestCodeList, instructionCodeList);
			for (InstructionCode instructionCode : instructionCodeList) {
				ctx1 = SnMapChannelHandlerContext.getMapping(instructionCode.getSn());
				if (ctx1 != null)
					ctx1.writeAndFlush(instructionCode);
				System.out.println("Client send instructionCode messsage to server : ---> " + instructionCode);
			}
		}
	}

	/**
	 * 定时任务获取当天用电量，每隔一个小时获取一次
	 */
	@Scheduled(cron = "*/3600 * * * * ?")
	public void electricityTask() {
		List<DeviceBindDetailInfo> eleDeviceList = deviceBindDetailInfoDao.getEleDevice(SchoolCode.schoolCode);
		if (eleDeviceList != null)
			for (DeviceBindDetailInfo eleDevice : eleDeviceList) {
				ctx2 = SnMapChannelHandlerContext.getMapping(eleDevice.getSn());
				if (ctx2 != null) {
					ctx2.writeAndFlush(buildEleCode(eleDevice));
					System.out.println("获取用电量 : ---> ");
				}
			}
	}

	/**
	 * 定时心跳检测
	 */
	@Scheduled(cron = "*/10 * * * * ?")
	public void heartBeat() {
		Map<String, ChannelHandlerContext> snMapChannelHandlerContext = SnMapChannelHandlerContext.getMap();
		InstructionCode heartBeatCode = new InstructionCode();
		heartBeatCode.setType((byte) 0x1e);
		System.out.println("现在连接数:"+snMapChannelHandlerContext.size());
		for (ChannelHandlerContext ctx : snMapChannelHandlerContext.values()) {
			if(!ctx.isRemoved()) {
			ctx.writeAndFlush(heartBeatCode);
			System.out.println("发送心跳检测：" + heartBeatCode);}
			else {
				ctx.close();
			}
		}
	}

	/**
	 * 定时任务获取正式指令
	 */
	@Scheduled(cron = "*/2 * * * * ?")
	public void getJkCode() {
		List<JkCode> jkCodeList = remoteService.getJkCodeBySchoolCode(SchoolCode.schoolCode);
		if (jkCodeList != null) {
			List<InstructionCode> instructionCodeList = new ArrayList<InstructionCode>();
			jkCodeToInstructionCode(jkCodeList, instructionCodeList);
			for (InstructionCode instructionCode : instructionCodeList) {
				ctx3 = SnMapChannelHandlerContext.getMapping(instructionCode.getSn());
				if (ctx3 != null) {
					ctx3.writeAndFlush(instructionCode);
				System.out.println("发送指令成功" + instructionCode);}
				else {
					System.out.println("该网关断线");
				}
			}
		}
	}

	/**
	 * 定时任务获取设备编号与临时id的绑定
	 */
	@Scheduled(cron = "*/2 * * * * ?")
	public void instructionCodeRemoteService() {
		try {
			List<TempReflect> tempReflectList = remoteService.getTempReflect(SchoolCode.schoolCode);
			if (tempReflectList != null)
				for (TempReflect tempReflect : tempReflectList) {
					DeviceBindDetailInfo deviceBindDetailInfo = deviceBindDetailInfoDao
							.getDeviceBindDetailInfoByTempId(tempReflect.getTempId());
					if (deviceBindDetailInfoDao
							.getDeviceBindDetailInfoByEquipmentCode(tempReflect.getEquipmentCode()) != null) {
						deviceBindDetailInfoDao.delDeviceBindDetailInfoByEquipmentCode(tempReflect.getEquipmentCode());
					}
					deviceBindDetailInfo.setEquipmentCode(tempReflect.getEquipmentCode());
					deviceBindDetailInfo.setEquipmentName(tempReflect.getEquipmentName());
					deviceBindDetailInfoDao.updateDeviceBindDetailInfo(deviceBindDetailInfo);
					remoteService.updateTempReflect(tempReflect);
				}
		} catch (Exception e) {
			System.out.println("发生异常，捕获");
		}

	}

	/**
	 * 测试指令转为网关标准指令
	 */
	private void jkTestCodeToInstructionCode(List<JkTestCode> jkTestCodeList,
			List<InstructionCode> instructionCodeList) {
		Date date = new Date();
		for (JkTestCode jkTestCode : jkTestCodeList) {
			System.out.println(jkTestCode);
			// 如果该指令设置的时间超过当前的时间的10s，则可能是网关服务端与集控云端网络延迟或者断网，可能会造成
			// 指令的堆积，所以超过10秒的指令抛弃
//			if (jkTestCode.getCreateTime().getTime() - (date.getTime()) <= 10000l) {
			InstructionCode instructionCode = new InstructionCode();
			DeviceBindDetailInfo deviceBindDetailInfo = deviceBindDetailInfoDao
					.getDeviceBindDetailInfoByTempId(jkTestCode.getTempId());
			if (deviceBindDetailInfo.getDevice() == (short) 0x0006) {
				instructionCode.setType((byte) 0x8c);
				// 红外设备
				instructionCode.setRedCode(
						"3001001b1a020301010102308d71004d01b202f0030f040705f88d72004d01b202d60329040705f8064d07b208d609290a070bf88d73004d02d88dff98");
				// 如果是传感器设备
			} else if (deviceBindDetailInfo.getDevice() == (short) 0x0000) {
				instructionCode.setType((byte) 0xad);
				// 如果是开 设置为3，反之2
				if (jkTestCode.getType().equals("开"))
					instructionCode.setState((byte) 0x03);
				else
					instructionCode.setState((byte) 0x02);
			} else { // 如果是开 设置为1，反之0
				instructionCode.setType((byte) 0x82);
				if (jkTestCode.getType().equals("开")) {
					System.out.println("kai");
					instructionCode.setState((byte) 0x01);
				} else {
					System.out.println("guang");
					instructionCode.setState((byte) 0x00);
				}
			}
			instructionCode.setSn(deviceBindDetailInfo.getSn());
			instructionCode.setAddress(deviceBindDetailInfo.getAddress());
			instructionCode.setEndpoint(deviceBindDetailInfo.getEndpoint());
			instructionCodeList.add(instructionCode);
//			}
			jkTestCode.setIsUpload(true);
			remoteService.updateJkTestCode(jkTestCode);
		}
	}

	/**
	 * 定时任务更新时间
	 */
	@Scheduled(cron = "*/600 * * * * ?")
	public void setDate() {
		DateInter.setDate(new Date());
	}

	/**
	 * 正式指令转为网关标准指令
	 */
	private void jkCodeToInstructionCode(List<JkCode> jkCodeList, List<InstructionCode> instructionCodeList) {
//		Date date = new Date();
		for (JkCode jkCode : jkCodeList) {
			// 判断指令是否超时
//			if (jkCode.getCreateTime().getTime() - (date.getTime()) <= 10000l) {
			Equip SnEquip = equipDao.getSnEquipByEquipmentCode(jkCode.getEquipmentCode());
			// 判断是否是网关设备
			if (SnEquip != null) {
				InstructionCode instructionCode = new InstructionCode();
				instructionCode.setSn(SnEquip.getSn());
				instructionCode.setType((byte) 0x9f);
				instructionCodeList.add(instructionCode);
			} else {
				System.out.println("不是添加设备指令");
				DeviceBindDetailInfo deviceBindDetailInfo = deviceBindDetailInfoDao
						.getDeviceBindDetailInfoByEquipmentCode(jkCode.getEquipmentCode());
				// 判断该指令的设备是否已经绑定
				if (deviceBindDetailInfo != null) {
					// 如果是传感器设备，则需要设置操作类型，将操作类型改变
					if (deviceBindDetailInfo.getDevice() == (short) 0x0000) {
						if (jkCode.getType().equals("开")) {
							jkCode.setType("传感器开");
							jkCode.setValue((byte) 0x03);
						} else if (jkCode.getType().equals("关")) {
							jkCode.setType("传感器关");
							jkCode.setValue((byte) 0x02);
						}
					}
					// 根据操作类型，获取网关操作码
					byte codetype = codeReflectDao.getCodeTypeByType(jkCode.getType());
					InstructionCode instructionCode = new InstructionCode();
					// 判断是否是红外操作
					if (codetype == (byte) 0x8c) {
//							String str=remoteService.getAirEquipByEquipmentCode(SchoolCode.schoolCode, deviceBindDetailInfo.getEquipmentCode());
//							JSONObject userJson = JSONObject.parseObject(str);
//							Equip equip = JSON.toJavaObject(userJson,Equip.class);
						instructionCode.setRedCode(
								"3001001b1a020301010102308d71004d01b202f0030f040705f88d72004d01b202d60329040705f8064d07b208d609290a070bf88d73004d02d88dff98");
						System.out.println("是红外操作指令");
					}
					instructionCode.setType(codetype);
					instructionCode.setSn(deviceBindDetailInfo.getSn());
					instructionCode.setAddress(deviceBindDetailInfo.getAddress());
					instructionCode.setEndpoint(deviceBindDetailInfo.getEndpoint());
					instructionCode.setState(jkCode.getValue());
					instructionCode.setValue(jkCode.getValue());
					instructionCodeList.add(instructionCode);
				} else {
					System.out.println("该指令的设备未绑定");
				}
			}
//			}
//		else {
//				System.out.println("指令超时");
//			}
			jkCode.setIsUpload(true);
			remoteService.updateJkCode(jkCode);
		}
	}

	/**
	 * 新建获取用电量指令
	 */
	private InstructionCode buildEleCode(DeviceBindDetailInfo eleDevice) {
		InstructionCode instructionCode = new InstructionCode();
		instructionCode.setSn(eleDevice.getSn());
		instructionCode.setAddress(eleDevice.getAddress());
		instructionCode.setEndpoint(eleDevice.getEndpoint());
		instructionCode.setType((byte) 0xa5);
		instructionCode.setStartTime(new SimpleDateFormat("ddMMyyyy").format(new Date()));
		instructionCode.setEndTime(new SimpleDateFormat("ddMMyyyy").format(new Date()));
		return instructionCode;
	}
}
