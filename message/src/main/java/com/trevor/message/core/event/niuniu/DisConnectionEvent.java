package com.trevor.message.core.event.niuniu;

import com.trevor.common.bo.SocketResult;
import com.trevor.message.bo.NiuniuData;
import com.trevor.message.bo.RoomData;
import com.trevor.message.bo.Task;
import com.trevor.message.core.event.BaseEvent;
import com.trevor.message.core.event.Event;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DisConnectionEvent extends BaseEvent implements Event {

    @Override
    public void execute(RoomData roomData, Task task) {
        NiuniuData data = (NiuniuData) roomData;
        Set<String> realPlayers = data.getRealPlayers();
        String playerId = task.getPlayId();
        Set<String> players = data.getPlayers();
        //如果是真正的玩家则广播消息断开连接的消息
        if (realPlayers.contains(playerId)) {
            data.getDisConnections().add(playerId);
            SocketResult res = new SocketResult(1001, playerId);
            socketService.broadcast(task.getRoomId(), res, players);
        }
    }
}
