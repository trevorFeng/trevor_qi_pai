package com.trevor.message.core.event.niuniu;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.trevor.common.bo.PaiXing;
import com.trevor.common.bo.Player;
import com.trevor.common.bo.RedisConstant;
import com.trevor.common.bo.SocketResult;
import com.trevor.common.domain.mysql.User;
import com.trevor.common.enums.GameStatusEnum;
import com.trevor.common.enums.RoomTypeEnum;
import com.trevor.common.enums.SpecialEnum;
import com.trevor.common.util.JsonUtil;
import com.trevor.message.bo.NiuniuData;
import com.trevor.message.bo.RoomData;
import com.trevor.message.bo.Task;
import com.trevor.message.core.event.BaseEvent;
import com.trevor.message.core.event.Event;
import com.trevor.message.socket.socketImpl.NiuniuSocket;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class JoinRoomEvent extends BaseEvent implements Event {

    @Override
    public void execute(RoomData roomData, Task task) {
        NiuniuData data = (NiuniuData) roomData;
        NiuniuSocket socket = (NiuniuSocket) task.getSocket();
        //房主是否开启好友管理功能
        Boolean isFriendManage = task.getIsFriendManage();
        //加入的玩家是否是房主的好友
        Boolean roomAuthFriendAllow = task.getRoomAuthFriendAllow();
        Set<Integer> special = data.getSpecial();

        User joinUser = task.getJoinUser();
        Integer realPlayersSize = data.getRealPlayers().size();
        Integer roomType = data.getRoomType();

        //房间规则检查
        SocketResult soc = checkRoom(isFriendManage, special, roomAuthFriendAllow
                , joinUser, realPlayersSize, roomType);
        if (soc.getHead() != null) {
            socket.directSendMessage(soc, socket.session);
            socket.close(socket.session);
            return;
        }

        //广播新人加入（给其他玩家发消息）
        soc.setHead(1000);
        String playerId = task.getPlayId();
        String roomId = task.getRoomId();
        Set<String> players = data.getPlayers();
        Map<String, Integer> totalScoreMap = data.getTotalScoreMap();
        if (!soc.getIsChiGuaPeople()) {
            soc.setTotalScore(totalScoreMap.get(playerId) == null ? 0 : totalScoreMap.get(playerId));
            socketService.broadcast(roomId, soc, players);
        }

        //删除自己的消息队列
        redisService.delete(RedisConstant.MESSAGES_QUEUE + playerId);
        //加入广播的队列
        socketService.join(socket);
        data.getPlayers().add(playerId);

        //给新人发消息
        Integer runingNum = Integer.valueOf(data.getRuningNum());
        Integer totalNum = Integer.valueOf(data.getTotalNum());
        //设置房间正在运行的局数
        soc.setRuningAndTotal((runingNum + 1) + "/" + totalNum);
        //不是吃瓜群众则加入到真正的玩家集合中并且删除自己的掉线状态\
        Set<String> disConnections = data.getDisConnections();
        if (!soc.getIsChiGuaPeople()) {
            //加入到真正的玩家中
            data.getRealPlayers().add(playerId);
            //删除自己掉线状态
            disConnections.remove(playerId);
        }
        //设置掉线的玩家
        soc.setDisConnectionPlayerIds(disConnections);
        //设置真正的玩家
        Set<String> realUserIds = data.getRealPlayers();
        Set<String> guanZhongUserIds = data.getGuanZhongs();
        soc.setPlayers(getRealRoomPlayerCount(realUserIds, guanZhongUserIds, totalScoreMap));
        //发送房间状态消息
        welcome(data, task, soc);
    }

    private SocketResult checkRoom(Boolean isFriendManage, Set<Integer> special, Boolean roomAuthFriendAllow
            , User joinUser, Integer realPlayersSize, Integer roomType) {
        if (isFriendManage) {
            //配置仅限好友
            if (special.contains(SpecialEnum.JUST_FRIENDS.getCode())) {
                //不是房主的好友
                if (!roomAuthFriendAllow) {
                    return new SocketResult(508);
                    //是房主的好友
                } else {
                    return dealCanSee(joinUser, special, realPlayersSize, roomType);
                }
            }
            //未配置仅限好友
            else {
                return dealCanSee(joinUser, special, realPlayersSize, roomType);
            }
            // 未开通
        } else {
            return dealCanSee(joinUser, special, realPlayersSize, roomType);
        }
    }

    /**
     * 处理是否可以观战
     *
     * @throws IOException
     */
    private SocketResult dealCanSee(User user, Set<Integer> special, Integer realPlayersSize, Integer roomType) {
        SocketResult socketResult = new SocketResult();
        socketResult.setUserId(String.valueOf(user.getId()));
        socketResult.setName(user.getAppName());
        socketResult.setPictureUrl(user.getAppPictureUrl());
        Boolean bo = realPlayersSize < RoomTypeEnum.getRoomNumByType(roomType);
        //允许观战
        if (special != null && special.contains(SpecialEnum.CAN_SEE.getCode())) {
            if (bo) {
                socketResult.setIsChiGuaPeople(Boolean.FALSE);
            } else {
                socketResult.setIsChiGuaPeople(Boolean.TRUE);
            }
            return socketResult;
            //不允许观战
        } else {
            if (bo) {
                socketResult.setIsChiGuaPeople(Boolean.FALSE);
                return socketResult;
            } else {
                return new SocketResult(509);
            }

        }
    }

    /**
     * 得到房间里真正的玩家
     *
     * @return
     */
    private List<Player> getRealRoomPlayerCount(Set<String> realUserIds, Set<String> guanZhongUserIds, Map<String, Integer> totalScoreMap) {
        List<Long> userIds = Lists.newArrayList();
        for (String s : realUserIds) {
            userIds.add(Long.valueOf(s));
        }
        List<User> users = userService.findUsersByIds(userIds);

//        List<User> users = Lists.newArrayList();
//        for (String userStr : userStrs) {
//            users.add(JsonUtil.parseJavaObject(userStr, User.class));
//        }

        List<Player> players = Lists.newArrayList();
        for (User user : users) {
            Player player = new Player();
            player.setUserId(user.getId());
            player.setName(user.getAppName());
            player.setPictureUrl(user.getAppPictureUrl());
            if (guanZhongUserIds.contains(String.valueOf(user.getId()))) {
                player.setIsGuanZhong(Boolean.TRUE);
            }
            players.add(player);
            player.setTotalScore(totalScoreMap.get(user.getId().toString()) == null ? 0 : totalScoreMap.get(user.getId().toString()));
        }
        return players;
    }

    /**
     * 欢迎玩家加入，发送房间状态信息
     */
    private void welcome(NiuniuData data, Task task, SocketResult socketResult) {
        socketResult.setHead(2002);
        String gameStatus = data.getGameStatus();
        String runingNum = data.getRuningNum();
        String userId = task.getPlayId();
        Set<String> readyPlayers = data.getReadyPlayMap().get(runingNum);
        //List<String> pokes_4 = data.getPokesMap().get(runingNum).get(userId).subList(0, 4);
        socketResult.setGameStatus(gameStatus);
        //设置准备的玩家
        if (Objects.equals(gameStatus, GameStatusEnum.READY.getCode()) ||
                Objects.equals(gameStatus, GameStatusEnum.READY_COUNT_DOWN_START.getCode()) ||
                Objects.equals(gameStatus, GameStatusEnum.READY_COUNT_DOWN_END.getCode())) {
            socketResult.setReadyPlayerIds(readyPlayers);
        }
        //设置玩家先发的4张牌
        else if (Objects.equals(gameStatus, GameStatusEnum.FA_FOUR_PAI.getCode())) {
            List<String> pokes_4 = data.getPokesMap().get(runingNum).get(userId).subList(0, 4);
            socketResult.setUserPokeList_4(pokes_4);
        }
        //设置抢庄的玩家
        else if (Objects.equals(gameStatus, GameStatusEnum.QIANG_ZHUANG_COUNT_DOWN_START.getCode()) ||
                Objects.equals(gameStatus, GameStatusEnum.QIANG_ZHUANG_COUNT_DOWN_END.getCode())) {
            List<String> pokes_4 = data.getPokesMap().get(runingNum).get(userId).subList(0, 4);
            socketResult.setUserPokeList_4(pokes_4);
            socketResult.setQiangZhuangMap(data.getQiangZhuangMap().get(runingNum));
        }
        //设置庄家
        else if (Objects.equals(gameStatus, GameStatusEnum.QIANG_ZHUANG_ZHUAN_QUAN.getCode())) {
            List<String> pokes_4 = data.getPokesMap().get(runingNum).get(userId).subList(0, 4);
            socketResult.setUserPokeList_4(pokes_4);
            socketResult.setZhuangJiaUserId(data.getZhuangJiaMap().get(runingNum));
        }
        //设置玩家下注信息
        else if (Objects.equals(gameStatus, GameStatusEnum.XIA_ZHU_COUNT_DOWN_START.getCode()) ||
                Objects.equals(gameStatus, GameStatusEnum.XIA_ZHU_COUNT_DOWN_END.getCode()) ||
                Objects.equals(gameStatus, GameStatusEnum.DEFAULT_XIA_ZHU.getCode())) {
            List<String> pokes_4 = data.getPokesMap().get(runingNum).get(userId).subList(0, 4);
            socketResult.setUserPokeList_4(pokes_4);
            socketResult.setZhuangJiaUserId(data.getZhuangJiaMap().get(runingNum));
            socketResult.setXianJiaXiaZhuMap(data.getXiaZhuMap().get(runingNum));
        }
        //设置最后一张牌
        else if (Objects.equals(gameStatus, GameStatusEnum.FA_ONE_PAI.getCode())) {
            setLastPoke(data, runingNum, socketResult);
        }
        //设置摊牌的玩家
        else if (Objects.equals(gameStatus, GameStatusEnum.TAN_PAI_COUNT_DOWN_START.getCode()) ||
                Objects.equals(gameStatus, GameStatusEnum.TAN_PAI_COUNT_DOWN_END.getCode())) {
            setLastPoke(data, runingNum, socketResult);
            socketResult.setTanPaiPlayerUserIds(data.getTanPaiMap().get(runingNum));
        }
        socketService.sendToUserMessage(userId, socketResult, task.getRoomId());
    }

    private void setLastPoke(NiuniuData data, String runingNum, SocketResult socketResult) {
        socketResult.setUserPokeMap_5(data.getPokesMap().get(runingNum));
        socketResult.setZhuangJiaUserId(data.getZhuangJiaMap().get(runingNum));
        socketResult.setXianJiaXiaZhuMap(data.getXiaZhuMap().get(runingNum));
        socketResult.setScoreMap(data.getRuningScoreMap().get(runingNum));
        Map<String, Integer> map = Maps.newHashMap();
        for (Map.Entry<String, PaiXing> entry : data.getPaiXingMap().get(runingNum).entrySet()) {
            map.put(entry.getKey(), entry.getValue().getPaixing());
        }
        socketResult.setPaiXing(map);
    }


}
