package com.trevor.message.core;

import com.trevor.message.bo.RoomData;
import com.trevor.message.bo.Task;
import com.trevor.message.bo.TaskFlag;
import com.trevor.message.core.event.niuniu.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class NiuniuCore {

    @Resource
    private DisConnectionEvent disConnectionEvent;

    @Resource
    private LeaveEvent leaveEvent;

    @Resource
    private JoinRoomEvent joinRoomEvent;

    @Resource
    private ReadyEvent readyEvent;

    @Resource
    private CountDownEvent countDownEvent;

    @Resource
    private FaPai4Event faPai4Event;

    @Resource
    private DefaultXiaZhuEvent defaultXiaZhuEvent;

    @Resource
    private FaPai1Event faPai1Event;

    @Resource
    private QiangZhuangEvent qiangZhuangEvent;

    @Resource
    private SelectZhuangJiaEvent selectZhuangJiaEvent;

    @Resource
    private XiaZhuEvent xiaZhuEvent;

    @Resource
    private TanPaiEvent tanPaiEvent;

    @Resource
    private StopOrContinueEvent stopOrContinueEvent;

    public void executNiuniu(Task task, RoomData roomData) {
        if (Objects.equals(task.getFlag(), TaskFlag.DIS_CONNECTION)) {
            disConnectionEvent.execute(roomData ,task);
        } else if (Objects.equals(task.getFlag(), TaskFlag.LEAVE)) {
            leaveEvent.execute(roomData, task);
        } else if (Objects.equals(task.getFlag(), TaskFlag.JOIN_ROOM)) {
            joinRoomEvent.execute(roomData ,task);
        } else if (Objects.equals(task.getFlag(), TaskFlag.READY)) {
            readyEvent.execute(roomData ,task);
        } else if (Objects.equals(task.getFlag(), TaskFlag.COUNT_DOWN)) {
            countDownEvent.execute(roomData ,task);
        }else if (Objects.equals(task.getFlag(), TaskFlag.FA_PAI_4)) {
            faPai4Event.execute(roomData ,task);
        }else if (Objects.equals(task.getFlag(), TaskFlag.DEFAULT_XIA_ZHU)) {
            defaultXiaZhuEvent.execute(roomData ,task);
        }else if (Objects.equals(task.getFlag(), TaskFlag.FA_PAI_1)) {
            faPai1Event.execute(roomData ,task);
        }else if (Objects.equals(task.getFlag(), TaskFlag.QIANG_ZHAUNG)) {
            qiangZhuangEvent.execute(roomData ,task);
        }else if (Objects.equals(task.getFlag(), TaskFlag.SELECT_ZHUANG_JIA)) {
            selectZhuangJiaEvent.execute(roomData ,task);
        }else if (Objects.equals(task.getFlag(), TaskFlag.XIA_ZHU)) {
            xiaZhuEvent.execute(roomData ,task);
        }else if (Objects.equals(task.getFlag(), TaskFlag.TAN_PAI)) {
            tanPaiEvent.execute(roomData ,task);
        }else if (Objects.equals(task.getFlag(), TaskFlag.STOP_OR_CONTINUE)) {
            stopOrContinueEvent.execute(roomData ,task);
        }
    }
}
