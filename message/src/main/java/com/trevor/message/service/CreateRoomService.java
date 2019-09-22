package com.trevor.message.service;

import com.trevor.common.bo.JsonEntity;
import com.trevor.common.bo.RedisConstant;
import com.trevor.common.bo.ResponseHelper;
import com.trevor.common.dao.mongo.NiuniuRoomParamMapper;
import com.trevor.common.dao.mysql.CardConsumRecordMapper;
import com.trevor.common.dao.mysql.PersonalCardMapper;
import com.trevor.common.dao.mysql.RoomMapper;
import com.trevor.common.domain.mongo.NiuniuRoomParam;
import com.trevor.common.domain.mysql.CardConsumRecord;
import com.trevor.common.domain.mysql.Room;
import com.trevor.common.enums.ConsumCardEnum;
import com.trevor.common.enums.GameStatusEnum;
import com.trevor.common.enums.MessageCodeEnum;
import com.trevor.common.service.RedisService;
import com.trevor.message.bo.NiuniuData;
import com.trevor.message.core.GameCore;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

@Service
public class CreateRoomService {

    @Resource
    private GameCore gameCore;

    @Resource
    private RedisService redisService;

    @Resource
    private RoomMapper roomMapper;

    @Resource
    private CardConsumRecordMapper cardConsumRecordMapper;

    @Resource
    private NiuniuRoomParamMapper niuniuRoomParamMapper;

    @Resource
    private PersonalCardMapper personalCardMapper;

    public JsonEntity<Long> createRoom(NiuniuRoomParam niuniuRoomParam, String userId) {
        Long playerId = Long.valueOf(userId);
        //判断玩家拥有的房卡数量是否超过消耗的房卡数
        Integer cardNumByUserId = personalCardMapper.findCardNumByUserId(playerId);
        Integer consumCardNum;
        if (Objects.equals(niuniuRoomParam.getConsumCardNum(), ConsumCardEnum.GAME_NUM_12_CARD_3.getCode())) {
            consumCardNum = ConsumCardEnum.GAME_NUM_12_CARD_3.getConsumCardNum();
            if (cardNumByUserId < ConsumCardEnum.GAME_NUM_12_CARD_3.getConsumCardNum()) {
                return ResponseHelper.withErrorInstance(MessageCodeEnum.USER_ROOMCARD_NOT_ENOUGH);
            }
        } else {
            consumCardNum = ConsumCardEnum.GAME_NUM_24_CARD_6.getConsumCardNum();
            if (cardNumByUserId < ConsumCardEnum.GAME_NUM_24_CARD_6.getConsumCardNum()) {
                return ResponseHelper.withErrorInstance(MessageCodeEnum.USER_ROOMCARD_NOT_ENOUGH);
            }
        }
        //生成房间，将房间信息存入数据库
        Integer totalNum;
        if (Objects.equals(consumCardNum, ConsumCardEnum.GAME_NUM_12_CARD_3.getConsumCardNum())) {
            totalNum = 12;
        } else {
            totalNum = 24;
        }
        Long currentTime = System.currentTimeMillis();
        Room room = new Room();
        room.generateRoomBase(niuniuRoomParam.getRoomType(), playerId, currentTime, totalNum);
        roomMapper.insertOne(room);

        //插入mongoDB
        niuniuRoomParam.setRoomId(room.getId());
        niuniuRoomParamMapper.save(niuniuRoomParam);

        //存入gameCore
        NiuniuData data = new NiuniuData();
        data.setRoomId(room.getId().toString());
        data.setRoomType(niuniuRoomParam.getRoomType());
        data.setRobZhuangType(niuniuRoomParam.getRobZhuangType());
        data.setBasePoint(niuniuRoomParam.getBasePoint());
        data.setRule(niuniuRoomParam.getRule());
        data.setXiazhu(niuniuRoomParam.getXiazhu());
        data.setSpecial(niuniuRoomParam.getSpecial());
        data.setPaiXing(niuniuRoomParam.getPaiXing());
        data.setTotalNum(totalNum.toString());
        data.setRuningNum("0");
        data.setGameStatus(GameStatusEnum.READY.getCode());
        gameCore.putRoomData(data ,room.getId().toString());

        //生成房卡消费记录
        CardConsumRecord cardConsumRecord = new CardConsumRecord();
        cardConsumRecord.generateCardConsumRecordBase(room.getId(), playerId, consumCardNum);
        cardConsumRecordMapper.insertOne(cardConsumRecord);

        //更新玩家的房卡数量信息
        personalCardMapper.updatePersonalCardNum(playerId, cardNumByUserId - consumCardNum);
        return ResponseHelper.createInstance(room.getId(), MessageCodeEnum.CREATE_SUCCESS);
    }
}
