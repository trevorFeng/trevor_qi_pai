package com.trevor.message.core;

import com.google.common.collect.Maps;
import com.trevor.message.bo.RoomData;
import com.trevor.message.bo.Task;
import com.trevor.message.bo.TaskFlag;
import com.trevor.message.core.event.niuniu.ReadyEvent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

@Service
public class GameCore {

    /**
     * 全部房间的游戏数据
     */
    private static Map<String, RoomData> map = Maps.newConcurrentMap();
    @Resource
    private ReadyEvent ready;


    public void putRoomData(RoomData roomData, String roomId) {
        map.put(roomId, roomData);
    }

    public void removeRoomData(String roomId) {
        map.remove(roomId);
    }

    public RoomData getRoomData(String roomId) {
        return map.get(roomId);
    }


    public void execut(Task task) {
        RoomData roomData = getRoomData(task.getRoomId());
        Integer roomType = roomData.getRoomType();
        if (Objects.equals(roomType, 1)) {
            executNiuniu(task, roomData);
        } else if (Objects.equals(roomType, 2)) {

        }
    }

    public void executNiuniu(Task task, RoomData roomData) {
        if (Objects.equals(task.getFlag(), TaskFlag.READY)) {
            ready.execute(roomData, task);
        } else if (Objects.equals(task.getFlag(), TaskFlag.COUNT_DOWN)) {

        }
    }
}
