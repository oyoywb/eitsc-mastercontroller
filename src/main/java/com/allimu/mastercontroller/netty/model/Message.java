package com.allimu.mastercontroller.netty.model;

public class Message {
	private Byte tag;
	private String sn;
	private Object object;
	private Byte type;
	private Short address;
	private Byte endpoint;
	private Byte state;
	private Short clusterId;
	private Float value;
	private Byte result;
	private Integer year;
	private Byte month;
	private Byte day;
	private Byte hour;
	private Byte min;
	private String name;
	private String password;
	public Byte getTag() {
		return tag;
	}
	public void setTag(Byte tag) {
		this.tag = tag;
	}
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public Object getObject() {
		return object;
	}
	public void setObject(Object object) {
		this.object = object;
	}
	public Byte getType() {
		return type;
	}
	public void setType(Byte type) {
		this.type = type;
	}
	public Short getAddress() {
		return address;
	}
	public void setAddress(Short address) {
		this.address = address;
	}
	public Byte getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(Byte endpoint) {
		this.endpoint = endpoint;
	}
	public Byte getState() {
		return state;
	}
	public void setState(Byte state) {
		this.state = state;
	}
	public Short getClusterId() {
		return clusterId;
	}
	public void setClusterId(Short clusterId) {
		this.clusterId = clusterId;
	}
	public Float getValue() {
		return value;
	}
	public void setValue(Float value) {
		this.value = value;
	}
	public Byte getResult() {
		return result;
	}
	public void setResult(Byte result) {
		this.result = result;
	}
	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}
	public Byte getMonth() {
		return month;
	}
	public void setMonth(Byte month) {
		this.month = month;
	}
	public Byte getDay() {
		return day;
	}
	public void setDay(Byte day) {
		this.day = day;
	}
	public Byte getHour() {
		return hour;
	}
	public void setHour(Byte hour) {
		this.hour = hour;
	}
	public Byte getMin() {
		return min;
	}
	public void setMin(Byte min) {
		this.min = min;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Override
	public String toString() {
		return "Message [tag=" + tag + ", sn=" + sn + ", object=" + object + ", type=" + type + ", address=" + address
				+ ", endpoint=" + endpoint + ", state=" + state + ", clusterId=" + clusterId + ", value=" + value
				+ ", result=" + result + ", year=" + year + ", month=" + month + ", day=" + day + ", hour=" + hour
				+ ", min=" + min + ", name=" + name + ", password=" + password + "]";
	}
	
	
}
