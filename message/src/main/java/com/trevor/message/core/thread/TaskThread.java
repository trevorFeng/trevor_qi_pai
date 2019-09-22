package com.trevor.message.core.thread;


import com.trevor.message.bo.Task;
import com.trevor.message.core.GameCore;

public class TaskThread implements Runnable {

    public Task task;

    public static GameCore gameCore;

    public TaskThread(Task task) {
        this.task = task;
    }

    @Override
    public void run() {
        gameCore.execut(task);
    }
}
