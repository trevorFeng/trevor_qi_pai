package com.trevor.message.core.detection;

import com.trevor.common.dao.mongo.NiuniuRoomParamMapper;
import com.trevor.common.dao.mysql.RoomMapper;
import com.trevor.common.domain.mongo.NiuniuRoomParam;
import com.trevor.common.domain.mysql.Room;
import com.trevor.common.enums.GameStatusEnum;
import com.trevor.message.bo.NiuniuData;
import com.trevor.message.core.GameCore;
import com.trevor.message.core.TaskQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 一句话描述该类作用:【】
 *
 * @author: trevor
 * @create: 2019-10-26 0:46
 **/
@Component
@Order(1)
@Slf4j
public class InitRoom implements ApplicationRunner {

    @Resource
    private RoomMapper roomMapper;

    @Resource
    private NiuniuRoomParamMapper niuniuRoomParamMapper;

    @Resource
    private TaskQueue taskQueue;

    @Resource
    private GameCore gameCore;

    /**
     * 初始化roomPoke到roomPokeMap中,初始化sessionsMap
     *
     * @param args
     */
    @Override
    public void run(ApplicationArguments args) throws Exception{
        System.out.println("这个是测试ApplicationRunner接口");
        List<Integer> statusList = new ArrayList<>();
        statusList.add(0);
        statusList.add(1);
        List<Room> rooms = roomMapper.findStatus(statusList);
        if (rooms.isEmpty()) {
            log.info("没有加载的房间");
            return;
        }
        List<Long> roomIds = rooms.stream().map(room -> room.getId()).collect(Collectors.toList());
        List<NiuniuRoomParam> niuniuParams = niuniuRoomParamMapper.findByRoomIds(roomIds);
        Map<Long, Integer> totalNumMap = rooms.stream().collect(Collectors.toMap(Room::getId, Room::getTotalNum));
        Map<Long, Integer> runingNumMap = rooms.stream().collect(Collectors.toMap(Room::getId, Room::getRuningNum));

        for (NiuniuRoomParam niuniuRoomParam : niuniuParams) {
            String roomId = niuniuRoomParam.getRoomId().toString();

            NiuniuData data = new NiuniuData();
            data.setRoomId(roomId);
            data.setRoomType(niuniuRoomParam.getRoomType());
            data.setRobZhuangType(niuniuRoomParam.getRobZhuangType());
            data.setBasePoint(niuniuRoomParam.getBasePoint());
            data.setRule(niuniuRoomParam.getRule());
            data.setXiazhu(niuniuRoomParam.getXiazhu());
            data.setSpecial(niuniuRoomParam.getSpecial());
            data.setPaiXing(niuniuRoomParam.getPaiXing() == null ? new HashSet<>() : niuniuRoomParam.getPaiXing());
            data.setTotalNum(totalNumMap.get(niuniuRoomParam.getRoomId()).toString());
            Integer runingNum = runingNumMap.get(niuniuRoomParam.getRoomId()) + 1;
            data.setRuningNum(runingNum.toString());
            data.setGameStatus(GameStatusEnum.READY.getCode());

            gameCore.putRoomData(data ,roomId);
            taskQueue.addQueue(roomId);
        }
    }


}
