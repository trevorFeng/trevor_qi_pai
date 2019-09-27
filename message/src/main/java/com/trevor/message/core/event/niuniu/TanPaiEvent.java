package com.trevor.message.core.event.niuniu;

import com.trevor.common.bo.SocketResult;
import com.trevor.common.enums.GameStatusEnum;
import com.trevor.message.bo.NiuniuData;
import com.trevor.message.bo.RoomData;
import com.trevor.message.bo.Task;
import com.trevor.message.core.event.BaseEvent;
import com.trevor.message.core.event.Event;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
public class TanPaiEvent extends BaseEvent implements Event {

    @Override
    public void execute(RoomData roomData, Task task) {
        NiuniuData data = (NiuniuData) roomData;
        String gameStatus = data.getGameStatus();
        String playerId = task.getPlayId();
        String roomId = task.getRoomId();
        //状态信息
        if (!Objects.equals(gameStatus, GameStatusEnum.TAN_PAI_COUNT_DOWN_START.getCode())) {
            socketService.sendToUserMessage(playerId, new SocketResult(-501), roomId);
            return;
        }
        String runingNum = data.getRuningNum();
        Set<String> readyPlayers = data.getReadyPlayMap().get(runingNum);
        if (!readyPlayers.contains(playerId)) {
            socketService.sendToUserMessage(playerId, new SocketResult(-503), roomId);
            return;
        }
        data.getTanPaiMap().putIfAbsent(runingNum, new HashSet<>());
        data.getTanPaiMap().get(runingNum).add(playerId);

        //广播摊牌的消息
        Set<String> players = data.getPlayers();
        SocketResult socketResult = new SocketResult();
        socketResult.setHead(1014);
        socketResult.setUserId(playerId);
        socketService.broadcast(roomId, socketResult, players);

        Integer readyPlayerSize = readyPlayers.size();
        Integer tanPaiSize = data.getTanPaiMap().get(runingNum).size();

        if (Objects.equals(readyPlayerSize, tanPaiSize)) {
            //删除摊牌倒计时监听器
            scheduleDispatch.removeCountDown(roomId);
            //添加继续或者停止事件
            taskQueue.addTask(roomId, Task.getStopOrContinue(roomId));
        }
    }
}
