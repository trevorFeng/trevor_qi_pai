package com.trevor.general.service;

import com.trevor.common.bo.JsonEntity;
import com.trevor.common.domain.mongo.NiuniuRoomParam;
import com.trevor.common.domain.mysql.User;
import com.trevor.general.exception.BizException;
import com.trevor.general.feign.MessageFeign;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author trevor
 * @date 2019/3/8 16:53
 */
@Service
public class CreateRoomService{

    @Resource
    private MessageFeign messageFeign;


    /**
     * 创建一个房间,返回主键,将房间放入Map中
     * @param niuniuRoomParam
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public JsonEntity<Long> createRoom(NiuniuRoomParam niuniuRoomParam , User user) {
        checkParm(niuniuRoomParam);
        return messageFeign.createRoom(niuniuRoomParam ,user.getId());
    }

    private void checkParm(NiuniuRoomParam niuniuRoomParameter){
        Integer roomType = niuniuRoomParameter.getRoomType();
        if (!Objects.equals(roomType ,1) && !Objects.equals(roomType ,2) && !Objects.equals(roomType ,3)) {
            throw new BizException(-200 ,"roomType 错误");
        }
        Integer robZhuangType = niuniuRoomParameter.getRobZhuangType();
        if (!Objects.equals(robZhuangType ,1) && !Objects.equals(robZhuangType ,2) &&
                !Objects.equals(robZhuangType ,3) && !Objects.equals(robZhuangType ,4)) {
            throw new BizException(-200 ,"robZhuangType 错误");
        }
        Integer basePoint = niuniuRoomParameter.getBasePoint();

    }


}
