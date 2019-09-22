package com.trevor.message.core.event;

import com.trevor.common.dao.mongo.PlayerResultMapper;
import com.trevor.common.dao.mysql.RoomMapper;
import com.trevor.common.service.RedisService;
import com.trevor.common.service.UserService;
import com.trevor.message.core.TaskQueue;
import com.trevor.message.core.schedule.ScheduleDispatch;
import com.trevor.message.service.SocketService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class BaseEvent {

    @Resource
    protected RedisService redisService;

    @Resource
    protected SocketService socketService;

    @Resource
    protected TaskQueue taskQueue;

    @Resource
    protected ScheduleDispatch scheduleDispatch;

    @Resource
    protected PlayerResultMapper playerResultMapper;

    @Resource
    protected RoomMapper roomMapper;

    @Resource
    protected UserService userService;

//    @Resource
//    protected UserFeignResult userFeignResult;

//    @Resource
//    protected PlayerResultFeignResult playerResultFeignResult;
//
//    @Resource
//    protected RoomFeignResult roomFeignResult;

}
