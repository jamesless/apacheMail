package com.sfpay.scout.alarm.mail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sfpay.scout.alarm.dao.AlarmSubscribeDao;
import com.sfpay.scout.alarm.domain.dto.AlarmContextDTO;
import com.sfpay.scout.alarm.mail.util.HtmlMailSender;
import com.sfpay.scout.alarm.mail.util.MailConstant;
import com.sfpay.scout.alarm.mail.util.MailSender;

/**
 * @author sfhq1588
 * @date 创建时间：2016年7月20日 下午3:56:41
 * @description
 * 邮件定时任务，准备与发送邮件
 */

@Service
public class SendMailJob extends MailConstant{

	@Resource
	private AlarmSubscribeDao alarmSubscribeDao;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SendMailJob.class);

	/**
	 * 获取告警信息，并发送邮件
	 */
	public void send() {
		Map<String, List<AlarmContextDTO>> alarMap = new HashMap<String, List<AlarmContextDTO>>();
		
		alarMap = getAlarmMsg(alarMap);

		sendRead(alarMap);
	}
	
	/**
	 * 准备邮件内容，并发送
	 * @param alarMap
	 */
	private void sendRead(Map<String, List<AlarmContextDTO>> alarMap) {
		StringBuffer contentBuffer =  new StringBuffer();	//邮件内容
		String bodyContent = "";
		String[] to = new String[1];	//收件人
		String subject;
		
		/**
		 * 遍历用户订阅的内容，并一封封的把告警信息发给订阅的用户
		 */
		for (String item : alarMap.keySet()) {
			try {
				LOGGER.info("发送邮件至: "+item);
				to[0] = alarMap.get(item).get(0).getEmail();	//获取收件人邮箱地址
				
				//追加拼接邮件内容
				for (AlarmContextDTO alarmContext : alarMap.get(item)) {
					contentBuffer.append("<br/>&nbsp;&nbsp;模块：");
					contentBuffer.append(alarmContext.getApplicationCode());
					contentBuffer.append("，告警信息 : ");
					contentBuffer.append(alarmContext.getAlarmLevel());
					contentBuffer.append("(");
					contentBuffer.append(alarmContext.getAmount());
					contentBuffer.append("次)");
				}
				
				contentBuffer.append("<br/><br/>&nbsp;&nbsp;<a href='https://scout.sf-pay.com'>进入scout告警监控系统</a>");
				
				//转到相应的码值，建立邮件对象
				bodyContent = new String((CONTENT+contentBuffer).getBytes("UTF-8"),"ISO-8859-1");
				subject = new String(SUBJECT.getBytes("ISO-8859-1"),"UTF-8");
				
				MailSender mailSender = new HtmlMailSender(subject, bodyContent, to);
				LOGGER.info("邮件准备完毕，准备发送");
				
				//调用邮件发送
				mailSender.send(getParam());
				contentBuffer.setLength(0);		//清楚缓存的字符串
				
				Thread.sleep(2000);		//每封邮件发送后休眠两秒
			} catch (Exception e) {
				LOGGER.error("用户： "+item+"在发送邮件的时候出现异常： "+e.toString());
			}
		}
	}
	
	/**
	 * 将properties的配置内容放进map中传递
	 * @return
	 */
	public Map<String,String> getParam(){
		Map<String, String> constantMap = new HashMap<>();
		constantMap.put("PROTOCOL", PROTOCOL);
		constantMap.put("HOST", HOST);
		constantMap.put("PORT", PORT);
		constantMap.put("FROM", FROM);
		constantMap.put("AUTH_USERNAME",AUTH_USERNAME);
		constantMap.put("AUTH_PASSWORD", AUTH_PASSWORD);
		constantMap.put("IS_AUTH", IS_AUTH);
		constantMap.put("IS_DEBUG", IS_DEBUG);
		constantMap.put("IS_SSL", IS_SSL);
		return constantMap;
	}

	/**
	 * 查询并遍历用户订阅的告警信息
	 * 在alarMap中，key是订阅用户的工号，value是该用户订阅的告警信息的List
	 * 如果在key相同，则追加告警信息
	 * @param alarMap
	 * @return 
	 */
	private Map<String, List<AlarmContextDTO>> getAlarmMsg(Map<String, List<AlarmContextDTO>> alarMap) {
		//准备要发送的告警日期
		String queryDateString =getQueryDate();
		
		List<AlarmContextDTO> alarmList;	//用户告警信息list
		List<AlarmContextDTO> alarmMsg = alarmSubscribeDao.queryAlarm(queryDateString);
		
		//遍历用户订阅的告警信息
		for (AlarmContextDTO context : alarmMsg) {
			if (alarMap.containsKey(context.getUserName())) {
				List<AlarmContextDTO> alarmInfoList = alarMap.get(context.getUserName());
				alarmInfoList.add(context);
				alarmInfoList = compareUtil(alarmInfoList);
			} else {
				alarmList = new ArrayList<>();
				alarmList.add(context);
				alarMap.put(context.getUserName(), alarmList);
			}
		}
		
		return alarMap;
	}
	
	/**
	 * 拼装昨天日期信息返回给调用者
	 * @return
	 */
	public String getQueryDate(){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());		//设置今天的日期
		calendar.add(Calendar.DAY_OF_MONTH, -1);		//获取昨天的日期
		
		String year = calendar.get(Calendar.YEAR)+"";
		String month = addHead(calendar.get(Calendar.MONTH)+1+"");	//外国佬的月份是从0开始的，加1吧
		String date = addHead(calendar.get(Calendar.DATE)+"");	
		String queryDateString = year+"-"+month+"-"+date;

		return queryDateString;
	}
	
	/**
	 * 给出的时间字符串中，如果位数小于2，则在前面补0
	 * @param timeStr
	 * @return
	 */
	public String addHead(String timeStr){
		if(timeStr.length() < 2){
			timeStr = "0"+timeStr;
		}
		return timeStr;
	}
	
	/**
	 * 根据告警等级排序，严重等级排最上
	 * @param list
	 * @return 
	 */
	public List<AlarmContextDTO> compareUtil(List<AlarmContextDTO> list){
		Collections.sort(list, new Comparator<AlarmContextDTO>() {
			@Override
			public int compare(AlarmContextDTO o1,AlarmContextDTO o2) {
				if(o1.getAlarmLevel().equals("一般")){
					return 1;
				}else if(o1.equals(o2)){
					return 0;
				}
				return -1;
			}
		});
		return list;
	}

}
