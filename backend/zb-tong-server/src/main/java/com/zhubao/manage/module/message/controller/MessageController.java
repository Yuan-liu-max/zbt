package com.zhubao.manage.module.message.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.message.dto.MessageSendDTO;
import com.zhubao.manage.module.message.entity.Message;
import com.zhubao.manage.module.message.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "站内信/私信")
@RestController
@RequestMapping("/messages")
@org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
public class MessageController {

    private final MessageService messageService;
    private final UserContextHolder userContextHolder;

    public MessageController(MessageService messageService, UserContextHolder userContextHolder) {
        this.messageService = messageService;
        this.userContextHolder = userContextHolder;
    }

    @ApiOperation("私信列表") @GetMapping
    public ApiResult<PageResult<Message>> list(@Valid PageDTO dto, @RequestParam(required = false) Integer isRead) {
        IPage<Message> r = messageService.page(userContextHolder.getUserId(), dto, isRead);
        return ApiResult.ok(PageResult.of(r));
    }

    @ApiOperation("未读数") @GetMapping("/unread-count")
    public ApiResult<Long> unreadCount() {
        return ApiResult.ok(messageService.unreadCount(userContextHolder.getUserId()));
    }

    @ApiOperation("标记已读") @PutMapping("/{id}/read")
    public ApiResult<Void> markRead(@PathVariable Long id) {
        messageService.markRead(id, userContextHolder.getUserId());
        return ApiResult.ok();
    }

    @ApiOperation("全部已读") @PutMapping("/read-all")
    public ApiResult<Void> markAllRead() {
        messageService.markAllRead(userContextHolder.getUserId());
        return ApiResult.ok();
    }

    @ApiOperation("发送私信") @PostMapping
    public ApiResult<Message> send(@Valid @RequestBody MessageSendDTO dto) {
        Message m = messageService.send(userContextHolder.getUserId(), dto.getReceiverId(), dto.getTitle(), dto.getContent());
        return ApiResult.ok(m);
    }
}
