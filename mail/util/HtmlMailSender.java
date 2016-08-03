package com.sfpay.scout.alarm.mail.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.resolver.DataSourceUrlResolver;

/** 
 * @author  sfhq1588 
 * @date 创建时间：2016年7月23日 下午4:12:54 
 * @description
 */
public class HtmlMailSender extends MailSender {

    public HtmlMailSender(String subject, String content, String[] to) {
        super(subject, content, to);
    }

    @Override
    protected Email createEmail() {
        return new ImageHtmlEmail();
    }

    @Override
    protected void setContent(Email email, String content) throws MalformedURLException, EmailException {
        ImageHtmlEmail imageHtmlEmail = (ImageHtmlEmail) email;
        imageHtmlEmail.setDataSourceResolver(new DataSourceUrlResolver(new URL("http://"), true));
        imageHtmlEmail.setHtmlMsg(content);
    }
}