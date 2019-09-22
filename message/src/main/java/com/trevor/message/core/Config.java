package com.trevor.message.core;

import com.trevor.message.core.schedule.CountDownImpl;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class Config {

    @Resource
    public void setTaskQueue(TaskQueue taskQueue) {
        CountDownImpl.taskQueue = taskQueue;
    }
}
