package com.trevor.message.core.event.niuniu;

import com.trevor.common.bo.SocketResult;
import com.trevor.common.enums.GameStatusEnum;
import com.trevor.message.bo.NiuniuData;
import com.trevor.message.bo.RoomData;
import com.trevor.message.bo.Task;
import com.trevor.message.core.event.BaseEvent;
import com.trevor.message.core.event.Event;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class XiaZhuEvent extends BaseEvent implements Event {

    @Override
    public void execute(RoomData roomData, Task task) {
        NiuniuData data = (NiuniuData) roomData;
        String gameStatus = data.getGameStatus();
        String playerId = task.getPlayId();
        String roomId = task.getRoomId();
        String runingNum = data.getRuningNum();
        Set<String> readyPlayers = data.getReadyPlayMap().get(runingNum);
        String zhuangJiaId = data.getZhuangJiaMap().get(runingNum);
        Set<String> players = data.getPlayers();
        //校验状态
        if (!Objects.equals(gameStatus, GameStatusEnum.XIA_ZHU_COUNT_DOWN_START.getCode())) {
            socketService.sendToUserMessage(playerId, new SocketResult(-501), roomId);
            return;
        }
        //校验是否是准备的玩家
        if (!readyPlayers.contains(playerId)) {
            socketService.sendToUserMessage(playerId, new SocketResult(-504), roomId);
            return;
        }
        //该玩家是否是闲家
        if (Objects.equals(zhuangJiaId, playerId)) {
            socketService.sendToUserMessage(playerId, new SocketResult(-505), roomId);
            return;
        }
        data.getXiaZhuMap().putIfAbsent(runingNum, new HashMap<>());
        data.getXiaZhuMap().get(runingNum).put(playerId, task.getXiaZhuBeiShu());
        //广播下注的消息
        socketService.broadcast(roomId,
                new SocketResult(1011, playerId, task.getXiaZhuBeiShu(), Boolean.TRUE)
                , players);

        Integer readyPlayerSize = readyPlayers.size();
        Integer xiaZhuSize = data.getXiaZhuMap().get(runingNum).size();
        if (Objects.equals(readyPlayerSize - 1, xiaZhuSize)) {
            //删除下注倒计时监听器
            scheduleDispatch.removeCountDown(roomId);
            //添加发一张牌事件
            taskQueue.addTask(roomId, Task.getNiuniuFaPai1(roomId));
        }
    }
}
