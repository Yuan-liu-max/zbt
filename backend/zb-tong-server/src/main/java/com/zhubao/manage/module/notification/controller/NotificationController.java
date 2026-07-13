package com.zhubao.manage.module.notification.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.notification.entity.Notification;
import com.zhubao.manage.module.notification.service.NotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@Api(tags = "消息通知")
@RestController
@RequestMapping("/notifications")
@org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserContextHolder userContextHolder;

    public NotificationController(NotificationService ns, UserContextHolder uc) { this.notificationService = ns; this.userContextHolder = uc; }

    @ApiOperation("通知列表") @GetMapping
    public ApiResult<PageResult<Notification>> list(@Valid PageDTO dto, @RequestParam(required = false) Integer isRead) {
        IPage<Notification> r = notificationService.page(userContextHolder.getUserId(), dto, isRead);
        return ApiResult.ok(PageResult.of(r)); }

    @ApiOperation("标记已读") @PutMapping("/{id}/read")
    public ApiResult<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id, userContextHolder.getUserId()); return ApiResult.ok();
    }

    @ApiOperation("全部已读") @PutMapping("/read-all")
    public ApiResult<Void> markAllRead() { notificationService.markAllRead(userContextHolder.getUserId()); return ApiResult.ok(); }

    @ApiOperation("未读数") @GetMapping("/unread-count")
    public ApiResult<Long> unreadCount() { return ApiResult.ok(notificationService.unreadCount(userContextHolder.getUserId())); }
}
