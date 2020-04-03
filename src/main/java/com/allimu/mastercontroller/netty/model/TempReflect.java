package com.allimu.mastercontroller.netty.model;

import java.io.Serializable;

/**
 * 
 * @author OuYang
 *	临时id与设备编号的绑定表
 */
public class TempReflect implements Serializable{
	private static final long serialVersionUID = 1L;
	private Long id;
	private Long schoolCode;
	private Long tempId;
	private String equipmentCode;
	private String equipmentName;
	
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
	public Long getTempId() {
		return tempId;
	}
	public void setTempId(Long tempId) {
		this.tempId = tempId;
	}
	public String getEquipmentCode() {
		return equipmentCode;
	}
	public void setEquipmentCode(String equipmentCode) {
		this.equipmentCode = equipmentCode;
	}
	public String getEquipmentName() {
		return equipmentName;
	}
	public void setEquipmentName(String equipmentName) {
		this.equipmentName = equipmentName;
	}
	

}
