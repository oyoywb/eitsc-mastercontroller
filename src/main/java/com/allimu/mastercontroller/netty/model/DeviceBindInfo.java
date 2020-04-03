package com.allimu.mastercontroller.netty.model;

import java.io.Serializable;

/**
 * 
 * @author OuYang
 *	临时绑定类
 */
public class DeviceBindInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	// 流水号id
	private Long id;
	// 学校编号
	private Long schoolCode;
	// 学校名称
	private String schoolName;
	// 教学楼编号
	private Long buildCode;
	// 教学楼名称
	private String buildName;
	// 课室编号
	private Long classRoomCode;
	// 课室名称
	private String classRoomName;
	// 设备类型
	private String equipmentType;
	// 是否上传到云端
	private Boolean isUpload;
	// 临时id
	private Long tempId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getSchoolCode() {
		return schoolCode;
	}

	public void setSchoolCode(Long schoolCode) {
		this.schoolCode = schoolCode;
	}

	public String getSchoolName() {
		return schoolName;
	}

	public void setSchoolName(String schoolName) {
		this.schoolName = schoolName;
	}

	public Long getBuildCode() {
		return buildCode;
	}

	public void setBuildCode(Long buildCode) {
		this.buildCode = buildCode;
	}

	public String getBuildName() {
		return buildName;
	}

	public void setBuildName(String buildName) {
		this.buildName = buildName;
	}

	public Long getClassRoomCode() {
		return classRoomCode;
	}

	public void setClassRoomCode(Long classRoomCode) {
		this.classRoomCode = classRoomCode;
	}

	public String getClassRoomName() {
		return classRoomName;
	}

	public void setClassRoomName(String classRoomName) {
		this.classRoomName = classRoomName;
	}

	public String getEquipmentType() {
		return equipmentType;
	}

	public void setEquipmentType(String equipmentType) {
		this.equipmentType = equipmentType;
	}

	public Boolean getIsUpload() {
		return isUpload;
	}

	public void setIsUpload(Boolean isUpload) {
		this.isUpload = isUpload;
	}

	public Long getTempId() {
		return tempId;
	}

	public void setTempId(Long tempId) {
		this.tempId = tempId;
	}
}
