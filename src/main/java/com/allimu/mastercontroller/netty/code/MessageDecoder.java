package com.allimu.mastercontroller.netty.code;



import com.allimu.mastercontroller.netty.config.ChannelHandlerContextMapSn;
import com.allimu.mastercontroller.netty.model.DeviceBindDetailInfo;
import com.allimu.mastercontroller.netty.model.Message;
import com.allimu.mastercontroller.netty.model.RedCodeRes;
import com.allimu.mastercontroller.netty.model.SchoolCode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.ReferenceCountUtil;

/**
 * 
 * @author OuYang Netty的LengthFieldBasedFrameDecoder解码器，它支持自动的TCP粘包和半包处理，
 *         只需要给出标识消息长度的字段偏移量和消息长度自身所占的字节数，Netty就能自动实现对半包的处理。
 */

public class MessageDecoder extends LengthFieldBasedFrameDecoder {

	public MessageDecoder() {
		super(1024 * 1024, 1, 1, 0, 0);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		// 对于业务解码器来说，调用父类LengthFieldBasedFrameDecoder的解码方法后，返回的就是整包消息或者为空，
		// 如果为空说明是个半包消息，直接返回继续由I/O线程读取后续的码流
		in = (ByteBuf) super.decode(ctx, in);
		if (in == null) {
			return null;
		}
		if (in.readableBytes() < 1) {
			throw new Exception("字节数不足");
		}
		Message message=new Message();
		byteToMessage(message, in, ctx.channel().remoteAddress().toString());
		ReferenceCountUtil.release(in);
		return message;
	}

	// 根据协议类型解码
	public void byteToMessage(Message message, ByteBuf in, String nodeIndex) {
		// read tag
		byte tag = in.readByte();
		message.setTag(tag);
		message.setSn(ChannelHandlerContextMapSn.getMapping(nodeIndex));
		// read length
		in.readByte();
		// 根据tag解码
		if (tag == (byte) 0x01) {
			reciveAllDevice(message, in);
		} else if (tag == (byte) 0x07 || tag == (byte) 0xb2) {
			reciveDeviceState(message, in);
		} else if (tag == (byte) 0x70) {
			reciveSensorData(message, in);
		} else if (tag == (byte) 0xb5) {
			reciveElectric(message, in);
		} else if (tag == (byte) 0x1f) {
			heartBeat(message, in);
		} else if (tag == (byte) 0xb6) {
			reciveAutoElectric(message, in);
		} else if (tag == (byte) 0x18) {
			reciveGatewayDatetime(message, in);
		} else if (tag == (byte) 0x19) {
			synresult(message, in);
		} else if (tag == (byte) 0x31) {
			getError(message, in);
		} else if (tag == (byte) 0x71) {
			getRedCodeRes(message, in);
		}
	}

	// 红外控制返回信息解码
	private void getRedCodeRes(Message message, ByteBuf in) {
		RedCodeRes redCodeRes = new RedCodeRes();
		// read address
		redCodeRes.setAddress(in.readShortLE());
		// read endpoint
		redCodeRes.setEndpoint(in.readByte());
		// read state
		redCodeRes.setState(in.readByte());

		redCodeRes.setSn(TypeConverter.byteBufToHexString(in.readBytes(in.readByte())));
		// set schoolcode
		redCodeRes.setSchoolCode(SchoolCode.schoolCode);
		message.setObject(redCodeRes);
	}

	// 设备绑定信息解码
	private void reciveAllDevice(Message message, ByteBuf in) {
		DeviceBindDetailInfo deviceBindDetailInfo = new DeviceBindDetailInfo();
		// read address
		deviceBindDetailInfo.setAddress(in.readShortLE());
		// read endpoint
		deviceBindDetailInfo.setEndpoint(in.readByte());
		// read profile
		in.readShort();
		// read device
		deviceBindDetailInfo.setDevice(in.readShortLE());
		// read areaid
		in.readByte();
		// read name(pcCode)
		in.readBytes(in.readByte()).release();
		// read state
		deviceBindDetailInfo.setState(in.readByte());
		// read ieee
		deviceBindDetailInfo.setIeee(in.readLong());

		// read sn
		deviceBindDetailInfo.setSn(TypeConverter.byteBufToHexString(in.readBytes(in.readByte())));
		// set isUpload

		deviceBindDetailInfo.setIsUpload(false);
		// set schoolcode
		deviceBindDetailInfo.setSchoolCode(SchoolCode.schoolCode);
		message.setObject(deviceBindDetailInfo);

	}

	// 设备状态信息解码
	private void reciveDeviceState(Message message, ByteBuf in) {
		// read address
		message.setAddress(in.readShortLE());
		// read endpoint
		message.setEndpoint(in.readByte());
		// read state
		message.setState(in.readByte());
	}

	// 传感器信息解码
	private void reciveSensorData(Message message, ByteBuf in) {
		// read address
		message.setAddress(in.readShortLE());
		// read endpoint
		message.setEndpoint(in.readByte());
		// read clusterid
		message.setClusterId(in.readShortLE());
		// read account
		in.readByte();
		// read attrid
		// 如果是粉尘传感器，重新赋值
		if (message.getClusterId() == (short) 0x0421) {
			message.setClusterId(in.readShortLE());
		} else {
			in.readShortLE();
		}
		// read type
		byte dataType = in.readByte();
		// read value 根据type赋值 无符号，低位在前
		if (dataType == (byte) 0x29) {
			message.setValue((in.readShortLE() & 0xffff) / 100f);
		} else if (dataType == (byte) 0x21) {
			message.setValue((in.readShortLE() & 0xffff) / 1f);
		} else if (dataType == (byte) 0x20) {
			message.setValue((in.readByte() & 0xff) / 1f);
		}
		System.out.println("环境状态解码中");
	}

	// 电箱或者移动插座耗电量解码
	private void reciveElectric(Message message, ByteBuf in) {
		// read address
		message.setAddress(in.readShortLE());
		// read endpoint
		message.setEndpoint(in.readByte());
		// read startTime
		in.readBytes(4).release();
		// read state ,保留2位小数
		message.setValue((in.readIntLE() & 0xffffffff) / 100f);
	}

	// 插座关闭时自动上传耗电量解码
	private void reciveAutoElectric(Message message, ByteBuf in) {
		// read address
		message.setAddress(in.readShortLE());
		// read endpoint
		message.setEndpoint(in.readByte());
		// read state ,保留2位小数
		message.setValue((in.readIntLE() & 0xffffffff) / 100f);
	}

	// 心跳信息解码
	private void heartBeat(Message message, ByteBuf in) {
		message.setResult(in.readByte());
	}

	// 网关时间解码
	private void reciveGatewayDatetime(Message message, ByteBuf in) {
		// read min
		message.setMin(in.readByte());
		// read hour
		message.setHour(in.readByte());
		// read day
		message.setDay(in.readByte());
		// read month
		message.setMonth(in.readByte());
		// read year
		message.setYear((in.readShortLE() & 0xffff));
	}

	// 同步网关时间返回信息解码
	private void synresult(Message message, ByteBuf in) {
		// read result
		message.setResult(in.readByte());
	}

	// 网关崩溃信息解码
	private void getError(Message message, ByteBuf in) {
		// read result
		message.setResult(in.readByte());
	}
}
