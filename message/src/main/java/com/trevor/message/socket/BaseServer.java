package com.trevor.message.socket;

import com.trevor.common.dao.mysql.FriendManageMapper;
import com.trevor.common.service.RedisService;
import com.trevor.common.service.RoomService;
import com.trevor.common.service.UserService;
import com.trevor.message.core.TaskQueue;
import com.trevor.message.service.SocketService;
import com.trevor.message.socket.service.NiuniuService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author trevor
 * @date 06/27/19 18:05
 */
@Component
public class BaseServer {

    protected static UserService userService;

    protected static SocketService socketService;

    protected static TaskQueue taskQueue;

    protected static NiuniuService niuniuService;

    protected static RoomService roomService;

    protected static FriendManageMapper friendManageMapper;

    protected static RedisService redisService;

    @Resource
    public void setUserService(UserService userService) {
        BaseServer.userService = userService;
    }

    @Resource
    public void setRoomSocketService(SocketService socketService) {
        BaseServer.socketService = socketService;
    }

    @Resource
    public void setTaskQueue(TaskQueue taskQueue) {
        BaseServer.taskQueue = taskQueue;
    }


    @Resource
    public void setNiuniuService(NiuniuService niuniuService) {
        BaseServer.niuniuService = niuniuService;
    }

    @Resource
    public void setRoomService(RoomService roomService) {
        BaseServer.roomService = roomService;
    }

    @Resource
    public void setFriendManageMapper(FriendManageMapper friendManageMapper) {
        BaseServer.friendManageMapper = friendManageMapper;
    }

    @Resource
    public void setRedisService(RedisService redisService) {
        BaseServer.redisService = redisService;
    }


}
