package com.trevor.message.core;

import com.google.common.collect.Maps;
import com.trevor.message.bo.RoomData;
import com.trevor.message.bo.Task;
import com.trevor.message.bo.TaskFlag;
import com.trevor.message.core.event.niuniu.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

@Service
public class GameCore {

    @Resource
    private NiuniuCore niuniuCore;

    /**
     * 全部房间的游戏数据
     */
    private static Map<String, RoomData> map = Maps.newConcurrentMap();

    public void putRoomData(RoomData roomData, String roomId) {
        map.put(roomId, roomData);
    }

    public void removeRoomData(String roomId) {
        map.remove(roomId);
    }

    public void execut(Task task) {
        RoomData roomData = map.get(task.getRoomId());
        if (roomData != null) {
            Integer roomType = roomData.getRoomType();
            if (Objects.equals(roomType, 1)) {
                niuniuCore.executNiuniu(task, roomData);
            } else if (Objects.equals(roomType, 2)) {

            }
        }
    }


}
