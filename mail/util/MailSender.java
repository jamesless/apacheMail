package com.sfpay.scout.alarm.mail.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * @author  sfhq1588 
 * @date 创建时间：2016年7月23日 下午4:13:31 
 * @description
 */
public abstract class MailSender{

	private static final Logger logger = LoggerFactory
			.getLogger(MailSender.class);

    // 创建 Email 对象（在子类中实现）
    private final Email email = createEmail();

    // 定义发送邮件的必填字段
    private final String subject;
    private final String content;
    private final String[] to;

    public MailSender(String subject, String content, String[] to) {
        this.subject = subject;
        this.content = content;
        this.to = Arrays.copyOf(to, to.length);
    }

    public void addCc(String[] cc) {
        try {
            if (ArrayUtil.isNotEmpty(cc)) {
                for (String address : cc) {
                    email.addCc(MailUtil.encodeAddress(address));
                }
            }
        } catch (EmailException e) {
            logger.error("错误：添加 CC 出错！", e);
        }
    }

    public void addBcc(String[] bcc) {
        try {
            if (ArrayUtil.isNotEmpty(bcc)) {
                for (String address : bcc) {
                    email.addBcc(MailUtil.encodeAddress(address));
                }
            }
        } catch (EmailException e) {
            logger.error("错误：添加 BCC 出错！", e);
        }
    }

    public void addAttachment(String path) {
        try {
            if (email instanceof MultiPartEmail) {
                MultiPartEmail multiPartEmail = (MultiPartEmail) email;
                EmailAttachment emailAttachment = new EmailAttachment();
                emailAttachment.setURL(new URL(path));
                emailAttachment.setName(path.substring(path.lastIndexOf('/') + 1));
                multiPartEmail.attach(emailAttachment);
            }
        } catch (MalformedURLException e) {
            logger.error("错误：创建 URL 出错！", e);
        } catch (EmailException e) {
            logger.error("错误：添加附件出错！", e);
        }
    }

    public final void send(Map<String,String> constantMap) {
        try {
            // 判断协议名是否为 smtp（暂时仅支持 smtp，未来可考虑扩展）
            if (!constantMap.get("PROTOCOL").equalsIgnoreCase("smtp")) {
                logger.error("错误：不支持该协议！目前仅支持 smtp 协议");
                return;
            }
            // 判断是否支持 SSL 连接
            if (constantMap.get("IS_SSL").equals("true")) {
                email.setSSLOnConnect(true);
            }
            // 设置 主机名 与 端口号
            email.setHostName(constantMap.get("HOST"));
            email.setSmtpPort(Integer.parseInt(constantMap.get("PORT")));
            // 判断是否进行身份认证
            if (constantMap.get("IS_AUTH").equals("true")) {
                email.setAuthentication(constantMap.get("AUTH_USERNAME"), constantMap.get("AUTH_PASSWORD"));
            }
            // 判断是否开启 Debug 模式
            if (constantMap.get("IS_DEBUG").equals("true")) {
                email.setDebug(true);
            }
            // 设置 From 地址
            if (StringUtil.isNotEmpty(constantMap.get("FROM"))) {
                email.setFrom(MailUtil.encodeAddress(constantMap.get("FROM")));
            }
            // 设置 To 地址
            if(to[0] != null && !to[0].equals("")){
            	for (String address : to) {
            		email.addTo(MailUtil.encodeAddress(address));
            	}
            }else{
            	logger.info("用户"+to[0]+"邮箱不存在！");
            	return;
            }
            // 设置主题
            email.setSubject(subject);
            // 设置内容（在子类中实现）
            setContent(email, content);
            // 发送邮件
            email.send();
        } catch (Exception e) {
            logger.error("错误：发送邮件出错！", e);
        }
    }

    protected abstract Email createEmail();

    protected abstract void setContent(Email email, String content) throws MalformedURLException, EmailException;
}