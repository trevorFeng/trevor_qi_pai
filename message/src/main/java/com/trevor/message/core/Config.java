package com.trevor.message.core;

import com.trevor.message.core.schedule.CountDownImpl;
import com.trevor.message.core.thread.TaskThread;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class Config {

    @Resource
    public void setTaskQueue(TaskQueue taskQueue) {
        CountDownImpl.taskQueue = taskQueue;
    }

    @Resource
    public void setGameCore(GameCore gameCore){
        TaskThread.gameCore = gameCore;
    }
}
