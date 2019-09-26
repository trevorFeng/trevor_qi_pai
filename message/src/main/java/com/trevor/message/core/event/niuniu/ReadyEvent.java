package com.trevor.message.core.event.niuniu;

import com.trevor.common.bo.SocketResult;
import com.trevor.common.enums.GameStatusEnum;
import com.trevor.common.util.NumberUtil;
import com.trevor.message.bo.CountDownFlag;
import com.trevor.message.bo.NiuniuData;
import com.trevor.message.bo.RoomData;
import com.trevor.message.bo.Task;
import com.trevor.message.core.event.BaseEvent;
import com.trevor.message.core.event.Event;
import com.trevor.message.core.schedule.CountDownImpl;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
public class ReadyEvent extends BaseEvent implements Event {


    @Override
    public void execute(RoomData roomData, Task task) {
        NiuniuData data = (NiuniuData) roomData;
        String userId = task.getPlayId();
        String roomId = task.getRoomId();
        //准备的人是否是真正的玩家
        if (!data.getRealPlayers().contains(task.getPlayId())) {
            SocketResult socketResult = new SocketResult(-502);
            socketService.sendToUserMessage(userId, socketResult, roomId);
            return;
        }
        //总局数
        String totalNum = data.getTotalNum();
        //当前局数
        String runingNum = data.getRuningNum();
        //房间里的玩家
        Set<String> players = data.getPlayers();
        //当前的房间状态
        String gameStatus = data.getGameStatus();
        //房间状态是不是准备状态
        if (!Objects.equals(gameStatus, GameStatusEnum.READY.getCode())) {
            //判断是否是最后一局，不是得话就准备下一局
            if (Objects.equals(runingNum, totalNum)) {
                SocketResult socketResult = new SocketResult(-501);
                socketService.sendToUserMessage(userId, socketResult, roomId);
                return;
            } else {
                String nextRuningNum = NumberUtil.stringFormatInteger(runingNum) + 1 + "";
                data.getReadyPlayMap().putIfAbsent(nextRuningNum, new HashSet<>());
                data.getReadyPlayMap().get(runingNum).add(userId);

                SocketResult socketResult = new SocketResult(1002);
                socketResult.setUserId(userId);

                socketService.broadcast(roomId, socketResult, players);
            }
        } else {
            data.getReadyPlayMap().putIfAbsent(runingNum ,new HashSet<>());
            data.getReadyPlayMap().get(runingNum).add(userId);
            //广播准备的消息
            SocketResult soc = new SocketResult();
            soc.setHead(1003);
            soc.setReadyPlayerIds(data.getReadyPlayMap().get(runingNum));
            socketService.broadcast(roomId, soc, players);

            //准备的人数超过两人
            Integer readyPlayerSize = data.getReadyPlayMap().get(runingNum).size();
            Integer realPlayerSize = data.getRealPlayers().size();

            if (readyPlayerSize == 2 && realPlayerSize > 2) {
                //注册准备倒计时监听器
                scheduleDispatch.addCountDown(new CountDownImpl(roomId, 5, CountDownFlag.NIUNIU_READY));
            } else if (Objects.equals(readyPlayerSize, realPlayerSize) && readyPlayerSize >= 2) {
                //移除监听器
                scheduleDispatch.removeCountDown(roomId);
                Task fapai4Task = Task.getNiuniuFaPai4(roomId);
                taskQueue.addTask(roomId, fapai4Task);
                return;
            }
        }

    }
}
