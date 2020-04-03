//package com.allimu.mastercontroller.netty.config;
//
//
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.channel.ChannelHandler.Sharable;
//import io.netty.channel.group.ChannelGroup;
//import io.netty.channel.group.DefaultChannelGroup;
//import io.netty.handler.timeout.IdleState;
//import io.netty.handler.timeout.IdleStateEvent;
//import io.netty.util.ReferenceCountUtil;
//import io.netty.util.concurrent.GlobalEventExecutor;
//
//
//import java.net.InetSocketAddress;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import com.allimu.mastercontroller.netty.code.MD5Tools;
//import com.allimu.mastercontroller.netty.model.InstructionCode;
//import com.allimu.mastercontroller.netty.model.Message;
//import com.allimu.mastercontroller.service.DeviceService;
//import com.allimu.mastercontroller.serviceImpl.DeviceServiceImpl;
//
//
///**
// * 
// * @author OuYang
// *	服务端握手安全认证
// */
//@Component
//@Sharable
//public class LoginHandler1 extends ChannelInboundHandlerAdapter {
//
//	private static Map<String, Boolean> nodeCheck = new ConcurrentHashMap<String, Boolean>();
//	private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
//	@Autowired
//	private DeviceService deviceService;
//
//	/*
//	 * 用于接入认证，首先根据客户端的源地址进行重复 登录判断，如果客户端己经登录成功，拒绝重复登录，以防止由于客户端重复登录导致的句
//	 * 柄泄漏。后通过ChanneIHandIercontext的ChanneI接凵获取客户端的InetSocketAddress
//	 * 地址，从中取得发送方的源地址信息，通过源地址进行白名单校验，校验通过握手成功，
//	 * 否则握手失败．最后通过buildResponse构造握手应答消息返回给客户端。 当发生异常关闭琏路的时候，需要将客户端的信息从登录注册表中去注册，以保证后
//	 * 续客户端可以重连成功。
//	 */
//	@Override
//	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//		Message message = (Message) msg;		
//		// 如果是握手请求消息，处理，其它消息透传
//		if (message.getType() != null) {
//			String name = message.getName();
//			String password = message.getPassword();
//			String sn = message.getSn();
//			System.out.println("客户端" + ctx.channel().remoteAddress() + "的登录消息 ：" + message);
//			// 登录验证，账号密码验证
//			if (name != null && password != null && name.equals("admin")
//					&& password.equals(MD5Tools.string2MD5("admin"))) {
//				String nodeIndex = ctx.channel().remoteAddress().toString();
//				// 重复登陆，拒绝
//				if (nodeCheck.containsKey(nodeIndex)) {
//					System.out.println("重复登录");
//					buildResponse((byte) 0x04, ctx);
//				} else {
//					boolean isOK = true;
//					if (isOK) {
//						buildResponse((byte) 0x00, ctx);
//					} else {
//						buildResponse((byte) 0x04, ctx);
//					}
//					if (isOK) {
//						// 设置ChannelHandlerContext与sn的映射
//						ChannelHandlerContextMapSn.setMapping(nodeIndex, sn);
//						// 设置ChannelHandlerContext与sn的映射
//						SnMapChannelHandlerContext.setMapping(sn, ctx);
//						System.out.println(sn+"    "+ctx);
//						// 设置该ChannelHandlerContext与已经登录
//						nodeCheck.put(nodeIndex, true);
//						// 登录成功后发送指令获取该网关下的设备
//						getAllDevice( sn);
//						
//					}
//				}
//			}
//			ReferenceCountUtil.release(msg);
//		} else {
//			ctx.fireChannelRead(msg);
//		}
//	}
//
//	private void buildResponse(byte result, ChannelHandlerContext ctx) {
//		InetSocketAddress address = (InetSocketAddress) ctx.channel().localAddress();
//		// 构建登录应答消息
//		InstructionCode loginResponse = new InstructionCode();
//		loginResponse.setType((byte) 0x40);
//		loginResponse.setIp(address.getAddress().getHostAddress());
//		loginResponse.setPort(address.getPort());
//		// 根据result设置type
//		if (result == 0x04) {
//			loginResponse.setValue((byte) 0x04);
//			ctx.writeAndFlush(loginResponse);
//			ctx.close();
//		} else if (result == 0x00) {
//			loginResponse.setValue((byte) 0x00);
//			ctx.writeAndFlush(loginResponse);
//		}
//		System.out.println("客户端" + ctx.channel().remoteAddress() + "的登录应答消息为 : " + loginResponse);
//	}
//
//	public void getAllDevice( String sn) {
//		ChannelHandlerContext ctx=SnMapChannelHandlerContext.getMapping(sn);
//		InstructionCode getAllDeviceCode = new InstructionCode();
//		getAllDeviceCode.setType((byte) 0x81);
//		getAllDeviceCode.setSn(sn);
//		System.out.println("发送指令获取客户端" + ctx.channel().remoteAddress() + "的设备绑定信息 :" + getAllDeviceCode);
//		ctx.writeAndFlush(getAllDeviceCode);
//	}
//
//	@Override
//	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
//		System.out.println(ctx.channel().remoteAddress() + " channelRegistered ");
//		super.channelRegistered(ctx);
//	}
//
//	@Override
//	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
//		System.out.println(ctx.channel().remoteAddress() + " channelUnregistered ");
//		//断网处理
//		deviceService.exceptionHandling(ChannelHandlerContextMapSn.getMapping(ctx.channel().remoteAddress().toString()));
//		// 移除映射
//		SnMapChannelHandlerContext.removeMapping(ChannelHandlerContextMapSn.getMapping(ctx.channel().remoteAddress().toString()));
//		ChannelHandlerContextMapSn.removeMapping(ctx.channel().remoteAddress().toString());
//		// 删除缓存
//		nodeCheck.remove(ctx.channel().remoteAddress().toString());
//		// 移除静态组
//		channels.remove(ctx.channel());
//		super.channelUnregistered(ctx);
//
//	}
//
//	@Override
//	public void channelActive(ChannelHandlerContext ctx) throws Exception {
//		System.out.println(ctx.channel().remoteAddress() + " channelActive ");
//		super.channelActive(ctx);
//	}
//
//	@Override
//	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//		System.out.println(ctx.channel().remoteAddress() + " channelInactive ");
//		super.channelInactive(ctx);
//	}
//
//	/**
//	 *
//	 * 服务端感知到断连事件之后，需要清空缓存的登录认证注册信息，以保证后续客户端 能够正常重连。
//	 * 
//	 * @param ctx
//	 * @param cause
//	 * @throws Exception
//	 */
//	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//		// 移除映射
//		SnMapChannelHandlerContext.removeMapping(ChannelHandlerContextMapSn.getMapping(ctx.channel().remoteAddress().toString()));
//		ChannelHandlerContextMapSn.removeMapping(ctx.channel().remoteAddress().toString());
//		// 删除缓存
//		nodeCheck.remove(ctx.channel().remoteAddress().toString());
//		// 移除静态组
//		channels.remove(ctx.channel());
//		cause.printStackTrace();
//		// 关闭链接
//		ctx.close();
//		ctx.fireExceptionCaught(cause);
//
//	}
//	
//	
//	@Override
//	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//		if(evt instanceof IdleStateEvent) {
//			IdleState state =((IdleStateEvent) evt).state();
//			if(state==IdleState.READER_IDLE) {
//				// 断网处理
//				deviceService
//						.exceptionHandling(ChannelHandlerContextMapSn.getMapping(ctx.channel().remoteAddress().toString()));
//				// 删除缓存
//				SnMapChannelHandlerContext
//						.removeMapping(ChannelHandlerContextMapSn.getMapping(ctx.channel().remoteAddress().toString()));
//				ChannelHandlerContextMapSn.removeMapping(ctx.channel().remoteAddress().toString());
//				nodeCheck.remove(ctx.channel().remoteAddress().toString());
//				// 移除静态组
//				channels.remove(ctx.channel());
//				ctx.channel().close();
//			}				
//		}else {
//			super.userEventTriggered(ctx, evt);
//		}		
//	}
//}
