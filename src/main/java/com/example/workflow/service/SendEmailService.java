package com.example.workflow.service;

/**
 * @author zhenwu
 */
public interface SendEmailService {

    /**
     * 发送简单邮件
     * @param to   收件人
     * @param subject 邮件主题
     * @param content 邮件正文
     */
    void sendSimpleEmail(String to, String subject, String content);
}
