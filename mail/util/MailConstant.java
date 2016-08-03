package com.sfpay.scout.alarm.mail.util;

import java.util.Calendar;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

public class MailConstant {
	
	@Value("${protocol}")
	public String PROTOCOL;			//协议
	@Value("${host}")
	public String HOST;				//邮箱主机地址
	@Value("${port}")
	public String PORT;				//端口
	@Value("${from}")
	public String FROM;				//发送者
	@Value("${auth_username}")
	public String AUTH_USERNAME;	//发送者用户名
	@Value("${auth_password}")
	public String AUTH_PASSWORD;	//发送者密码
	@Value("${subject}")
	public String SUBJECT;			//标题

	public String IS_AUTH = "true";		//是否验证
	public String IS_DEBUG = "false";	//是否开启debug模式
	public String IS_SSL = "false";		//是否启用SSL
	
	public String CUR_DATE = Calendar.getInstance().get(Calendar.DATE) + "";		//当前日期
	public String CONTENT = "Hi Dear All:<br/>&nbsp;&nbsp;以下是您订阅的" + CUR_DATE + "日的告警信息<br/>";	//邮件寒暄语

}