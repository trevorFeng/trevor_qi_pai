package com.trevor.message.core.event.niuniu;

import com.trevor.common.bo.SocketResult;
import com.trevor.common.enums.GameStatusEnum;
import com.trevor.message.bo.*;
import com.trevor.message.core.event.BaseEvent;
import com.trevor.message.core.event.Event;
import com.trevor.message.core.schedule.CountDownImpl;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

@Service
public class CountDownEvent extends BaseEvent implements Event {


    @Override
    public void execute(RoomData roomData, Task task) {
        String roomId = task.getRoomId();
        NiuniuData data = (NiuniuData) roomData;

        SocketResult socketResult = new SocketResult();
        setHead(socketResult ,task.getNiuniuCountDownFg());
        //改变房间状态
        if (Objects.equals(task.getCountDown(), task.getTotalCountDown())) {
            changeGameStatusStart(data, task, socketResult);
        } else if (Objects.equals(task.getCountDown(), CountDownNum.ONE)) {
            changeGameStatusEnd(data, task, socketResult);
        }

        socketResult.setCountDown(task.getCountDown());
        //房间里的玩家
        Set<String> players = data.getPlayers();
        socketService.broadcast(roomId, socketResult, players);

        if (Objects.equals(task.getCountDown(), CountDownNum.ONE)) {
            //倒计时为1，删除倒计时监听器
            scheduleDispatch.removeCountDown(task.getRoomId());
            //准备的倒计时结束，加入发4张牌的事件
            if (Objects.equals(task.getNiuniuCountDownFg(), CountDownFlag.NIUNIU_READY)) {
                Task faPai4Task = Task.getNiuniuFaPai4(roomId);
                taskQueue.addTask(roomId, faPai4Task);
                //抢庄的倒计时结束，加入选择庄家事件
            } else if (Objects.equals(task.getNiuniuCountDownFg(), CountDownFlag.NIUNIU_QIANG_ZHUANG)) {
                Task qiangZhuangTask = Task.getNiuniuSelectZhuangJia(roomId);
                taskQueue.addTask(roomId, qiangZhuangTask);
                //下注倒计时结束，加入默认下注事件
            } else if (Objects.equals(task.getNiuniuCountDownFg(), CountDownFlag.XIA_ZHU)) {
                Task qiangZhuangTask = Task.getNiuniuDefaultXiaZhu(roomId);
                taskQueue.addTask(roomId, qiangZhuangTask);
                //摊牌倒计时结束，加入是否继续事件
            } else if (Objects.equals(task.getNiuniuCountDownFg(), CountDownFlag.TAN_PAI)) {
                Task qiangZhuangTask = Task.getStopOrContinue(roomId);
                taskQueue.addTask(roomId, qiangZhuangTask);
                //注册下注倒计时
            }else if (Objects.equals(task.getNiuniuCountDownFg(), CountDownFlag.ZHUAN_QUAN)) {
                scheduleDispatch.addCountDown(new CountDownImpl(roomId, CountDownNum.TWENTY, CountDownFlag.XIA_ZHU));
            }
        }
    }

    private void setHead(SocketResult socketResult ,String countDownFlag){
        if (Objects.equals(countDownFlag, CountDownFlag.NIUNIU_READY)) {
            socketResult.setHead(1002);
        } else if (Objects.equals(countDownFlag, CountDownFlag.NIUNIU_QIANG_ZHUANG)) {
            socketResult.setHead(1005);
        } else if (Objects.equals(countDownFlag, CountDownFlag.XIA_ZHU)) {
            socketResult.setHead(1007);
        } else if (Objects.equals(countDownFlag, CountDownFlag.TAN_PAI)) {
            socketResult.setHead(1009);
        }else if (Objects.equals(countDownFlag, CountDownFlag.ZHUAN_QUAN)) {
            socketResult.setHead(1021);
        }
    }

    /**
     * 改变房间状态
     *
     * @param data
     * @param task
     * @param socketResult
     */
    private void changeGameStatusStart(NiuniuData data, Task task, SocketResult socketResult) {
        if (Objects.equals(task.getNiuniuCountDownFg(), CountDownFlag.NIUNIU_READY)) {
            data.setGameStatus(GameStatusEnum.READY_COUNT_DOWN_START.getCode());
            socketResult.setGameStatus(GameStatusEnum.READY_COUNT_DOWN_START.getCode());
        } else if (Objects.equals(task.getNiuniuCountDownFg(), CountDownFlag.NIUNIU_QIANG_ZHUANG)) {
            data.setGameStatus(GameStatusEnum.QIANG_ZHUANG_COUNT_DOWN_START.getCode());
            socketResult.setGameStatus(GameStatusEnum.QIANG_ZHUANG_COUNT_DOWN_START.getCode());
        } else if (Objects.equals(task.getNiuniuCountDownFg(), CountDownFlag.XIA_ZHU)) {
            data.setGameStatus(GameStatusEnum.XIA_ZHU_COUNT_DOWN_START.getCode());
            socketResult.setGameStatus(GameStatusEnum.XIA_ZHU_COUNT_DOWN_START.getCode());
        } else if (Objects.equals(task.getNiuniuCountDownFg(), CountDownFlag.TAN_PAI)) {
            data.setGameStatus(GameStatusEnum.TAN_PAI_COUNT_DOWN_START.getCode());
            socketResult.setGameStatus(GameStatusEnum.TAN_PAI_COUNT_DOWN_START.getCode());
        }
    }

    private void changeGameStatusEnd(NiuniuData data, Task task, SocketResult socketResult) {
        if (Objects.equals(task.getNiuniuCountDownFg(), CountDownFlag.NIUNIU_READY)) {
            data.setGameStatus(GameStatusEnum.READY_COUNT_DOWN_END.getCode());
            socketResult.setGameStatus(GameStatusEnum.READY_COUNT_DOWN_END.getCode());
        } else if (Objects.equals(task.getNiuniuCountDownFg(), CountDownFlag.NIUNIU_QIANG_ZHUANG)) {
            data.setGameStatus(GameStatusEnum.QIANG_ZHUANG_COUNT_DOWN_END.getCode());
            socketResult.setGameStatus(GameStatusEnum.QIANG_ZHUANG_COUNT_DOWN_END.getCode());
        } else if (Objects.equals(task.getNiuniuCountDownFg(), CountDownFlag.XIA_ZHU)) {
            data.setGameStatus(GameStatusEnum.XIA_ZHU_COUNT_DOWN_END.getCode());
            socketResult.setGameStatus(GameStatusEnum.XIA_ZHU_COUNT_DOWN_END.getCode());
        } else if (Objects.equals(task.getNiuniuCountDownFg(), CountDownFlag.TAN_PAI)) {
            data.setGameStatus(GameStatusEnum.TAN_PAI_COUNT_DOWN_END.getCode());
            socketResult.setGameStatus(GameStatusEnum.TAN_PAI_COUNT_DOWN_END.getCode());
        }
    }


}
