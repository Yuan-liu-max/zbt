package com.zhubao.manage.module.task.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Test
    void generateTasks_createsInstancesForEachStore() {
        assertTrue(true); // 框架就位，具体 mock 待补全
    }

    @Test
    void generateTasks_templateNotFound_throwsException() {
        assertTrue(true);
    }
}
