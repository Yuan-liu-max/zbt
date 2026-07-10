package com.zhubao.manage.module.notification.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 短信推送通道（预留）
 *
 * TODO: 接入阿里云/腾讯云短信 SDK
 *   1. 配置 accessKey + secretKey
 *   2. 注册短信签名和模板
 *   3. 调用 SMS API 发送
 */
public class SMSChannel {

    private static final Logger log = LoggerFactory.getLogger(SMSChannel.class);

    public void send(String phone, String templateCode, String params) {
        log.info("[短信-预留] 发送短信: phone={}, template={}, params={}", phone, templateCode, params);
        // TODO: 实现短信发送
    }
}
