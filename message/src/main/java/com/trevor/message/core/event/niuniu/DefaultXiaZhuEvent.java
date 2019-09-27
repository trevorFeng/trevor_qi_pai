package com.trevor.message.core.event.niuniu;

import com.google.common.collect.Maps;
import com.trevor.common.bo.SocketResult;
import com.trevor.common.enums.GameStatusEnum;
import com.trevor.message.bo.NiuniuData;
import com.trevor.message.bo.RoomData;
import com.trevor.message.bo.Task;
import com.trevor.message.core.event.BaseEvent;
import com.trevor.message.core.event.Event;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 默认下注事件
 */
@Service
public class DefaultXiaZhuEvent extends BaseEvent implements Event {


    @Override
    public void execute(RoomData roomData, Task task) {
        NiuniuData data = (NiuniuData) roomData;
        String rungingNum = data.getRuningNum();
        String roomId = data.getRoomId();
        Set<String> players = data.getPlayers();
        Set<String> readyPlayers = data.getReadyPlayMap().get(rungingNum);
        //已经下注的玩家
        data.getXiaZhuMap().putIfAbsent(rungingNum ,new HashMap<>());
        Collection<Integer> xiaZhuPlayers = data.getXiaZhuMap().get(rungingNum).values();
        String zhuangJiaPlayerId = data.getZhuangJiaMap().get(rungingNum);
        Map<String, Integer> map = Maps.newHashMap();
        for (String s : readyPlayers) {
            if (!Objects.equals(s, zhuangJiaPlayerId) && !xiaZhuPlayers.contains(s)) {
                map.put(s, 1);
            }
        }
        if (!map.isEmpty()) {
            data.setGameStatus(GameStatusEnum.DEFAULT_XIA_ZHU.getCode());
            SocketResult soc = new SocketResult();
            soc.setHead(1020);
            soc.setXianJiaXiaZhuMap(map);
            soc.setGameStatus(GameStatusEnum.DEFAULT_XIA_ZHU.getCode());
            socketService.broadcast(roomId, soc, players);
            taskQueue.addTask(roomId, Task.getNiuniuFaPai1(roomId));
        }
    }
}
