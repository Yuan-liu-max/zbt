package com.zhubao.manage.module.notification.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 企业微信推送通道（预留）
 *
 * TODO: 接入企业微信 API
 *   1. 配置 corpid + corpsecret
 *   2. 获取 access_token
 *   3. 调用 /cgi-bin/message/send 推送应用消息
 */
public class WeComChannel {

    private static final Logger log = LoggerFactory.getLogger(WeComChannel.class);

    public void send(String userId, String title, String content) {
        log.info("[企业微信-预留] 推送消息: userId={}, title={}", userId, title);
        // TODO: 实现企业微信消息推送
    }
}
