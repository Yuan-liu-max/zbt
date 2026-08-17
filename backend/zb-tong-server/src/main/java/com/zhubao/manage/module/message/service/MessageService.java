package com.zhubao.manage.module.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.module.message.entity.Message;
import com.zhubao.manage.module.message.mapper.MessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private final MessageMapper messageMapper;

    public MessageService(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Transactional(readOnly = true)
    public IPage<Message> page(Long receiverId, PageDTO dto, Integer isRead) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<Message>()
                .eq(receiverId != null, Message::getReceiverId, receiverId);
        if (isRead != null) w.eq(Message::getIsRead, isRead);
        w.orderByDesc(Message::getCreatedAt);
        return messageMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), w);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long receiverId) {
        return messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getReceiverId, receiverId)
                        .eq(Message::getIsRead, 0));
    }

    @Transactional
    public void markRead(Long id, Long currentUserId) {
        Message m = messageMapper.selectById(id);
        if (m == null) return;
        if (!m.getReceiverId().equals(currentUserId)) {
            throw new BusinessException(403, "无权操作此消息");
        }
        m.setIsRead(1);
        m.setReadAt(LocalDateTime.now());
        messageMapper.updateById(m);
    }

    @Transactional
    public void markAllRead(Long receiverId) {
        List<Message> list = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getReceiverId, receiverId)
                        .eq(Message::getIsRead, 0));
        for (Message m : list) {
            m.setIsRead(1);
            m.setReadAt(LocalDateTime.now());
            messageMapper.updateById(m);
        }
    }

    /** 发送私信（senderId 为空表示系统消息） */
    @Transactional
    public Message send(Long senderId, Long receiverId, String title, String content) {
        Message m = new Message();
        m.setSenderId(senderId);
        m.setReceiverId(receiverId);
        m.setTitle(title);
        m.setContent(content);
        m.setIsRead(0);
        messageMapper.insert(m);
        return m;
    }
}
