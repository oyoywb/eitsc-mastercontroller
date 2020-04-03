package com.allimu.mastercontroller.netty.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author OuYang 集控测试指令类
 */
public class JkTestCode implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	private Long schoolCode;
	private Long tempId;
	private String type;
	private Byte value;
	private Boolean isUpload;
	private Date createTime;

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Byte getValue() {
		return value;
	}

	public void setValue(Byte value) {
		this.value = value;
	}

	public Boolean getIsUpload() {
		return isUpload;
	}

	public void setIsUpload(Boolean isUpload) {
		this.isUpload = isUpload;
	}

	@Override
	public String toString() {
		return "JkTestCode [id=" + id + ", schoolCode=" + schoolCode + ", tempId=" + tempId + ", type=" + type
				+ ", value=" + value + ", isUpload=" + isUpload + ", createTime=" + createTime + "]";
	}

}
