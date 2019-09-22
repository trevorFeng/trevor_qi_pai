package com.trevor.message.socket.service;

import com.trevor.message.bo.SocketMessage;
import com.trevor.message.bo.Task;
import com.trevor.message.core.TaskQueue;
import com.trevor.message.socket.socketImpl.NiuniuSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class NiuniuService {

    @Resource
    private TaskQueue taskQueue;


    /**
     * 处理准备的消息
     *
     * @param roomId
     */
    public void dealReadyMessage(String roomId, NiuniuSocket socket) {
        Task task = Task.getNiuniuReady(roomId, socket.userId);
        taskQueue.addTask(roomId, task);
    }

    /**
     * 处理抢庄的消息
     *
     * @param roomId
     */
    public void dealQiangZhuangMessage(String roomId, NiuniuSocket socket, SocketMessage socketMessage) {
        Task task = Task.getNiuniuQiangZhuang(roomId, socket.userId, socketMessage.getQiangZhuangMultiple());
        taskQueue.addTask(roomId, task);
    }

    /**
     * 处理闲家下注的消息
     *
     * @param roomId
     */
    public void dealXiaZhuMessage(String roomId, NiuniuSocket socket, SocketMessage socketMessage) {
        Task task = Task.getNiuniuXiaZhu(roomId, socket.userId, socketMessage.getXianJiaMultiple());
        taskQueue.addTask(roomId, task);
    }

    /**
     * 处理摊牌的消息
     *
     * @param roomId
     */
    public void dealTanPaiMessage(String roomId, NiuniuSocket socket) {
        Task task = Task.getNiuniuTanPai(roomId, socket.userId);
        taskQueue.addTask(roomId, task);
    }
}
