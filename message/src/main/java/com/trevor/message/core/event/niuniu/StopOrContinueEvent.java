package com.trevor.message.core.event.niuniu;

import com.trevor.common.bo.PaiXing;
import com.trevor.common.bo.SocketResult;
import com.trevor.common.domain.mongo.PlayerResult;
import com.trevor.common.domain.mysql.User;
import com.trevor.common.enums.GameStatusEnum;
import com.trevor.message.bo.NiuniuData;
import com.trevor.message.bo.RoomData;
import com.trevor.message.bo.Task;
import com.trevor.message.core.event.BaseEvent;
import com.trevor.message.core.event.Event;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StopOrContinueEvent extends BaseEvent implements Event {

    @Override
    public void execute(RoomData roomData, Task task) {
        NiuniuData data = (NiuniuData) roomData;
        String roomId = data.getRoomId();
        Set<String> players = data.getPlayers();

        data.setGameStatus(GameStatusEnum.JIE_SUAN.getCode());
        SocketResult soc = new SocketResult(1012);
        soc.setGameStatus(GameStatusEnum.JIE_SUAN.getCode());

        socketService.broadcast(roomId, soc, players);

        //保存结果
        List<PlayerResult> playerResults = generatePlayerResults(roomId, data);
        playerResultMapper.saveAll(playerResults);

        Integer runingNum = Integer.valueOf(data.getRuningNum());
        Integer totalNum = Integer.valueOf(data.getTotalNum());
        Boolean isOver = Objects.equals(runingNum, totalNum);

        //结束
        if (isOver) {
            roomMapper.updateStatus(Long.valueOf(roomId), 2, runingNum);
            SocketResult socketResult = new SocketResult(1013);
            socketResult.setGameStatus(GameStatusEnum.STOP.getCode());
            socketResult.setTanPaiPlayerUserIds(data.getTanPaiMap().get(runingNum));

            socketService.broadcast(roomId, socketResult, data.getPlayers());
            socketService.stopRoom(players ,roomId);
        } else {
            Integer next = runingNum + 1;

            roomMapper.updateRuningNum(Long.valueOf(roomId), runingNum);

            data.setGameStatus(GameStatusEnum.READY.getCode());
            data.setRuningNum(next.toString());

            SocketResult socketResult = new SocketResult();
            socketResult.setHead(1016);
            socketResult.setTanPaiPlayerUserIds(data.getTanPaiMap().get(runingNum));
            socketResult.setRuningAndTotal(next + "/" + totalNum);
            socketService.broadcast(roomId, socketResult, data.getPlayers());
        }
    }

    private List<PlayerResult> generatePlayerResults(String roomId, NiuniuData data) {
        Long entryDatetime = System.currentTimeMillis();
        String runingNum = data.getRuningNum();
        Map<String, Integer> scoreMap = data.getRuningScoreMap().get(runingNum);
        Set<String> readyPlayerStr = data.getReadyPlayMap().get(runingNum);
        List<Long> readyPlayerLong = readyPlayerStr.stream().map(s -> Long.valueOf(s)).collect(Collectors.toList());
        List<User> users = userService.findUsersByIds(readyPlayerLong);
        String zhuangJiaId = data.getZhuangJiaMap().get(runingNum);
        Map<String, Integer> totalScoreMap = data.getTotalScoreMap();
        Map<String, List<String>> pokesMap = data.getPokesMap().get(runingNum);
        Map<String, PaiXing> paiXingMap = data.getPaiXingMap().get(runingNum);
        List<PlayerResult> playerResults = new ArrayList<>();
        for (User user : users) {
            PlayerResult playerResult = new PlayerResult();
            Long userId = user.getId();
            String userIdStr = String.valueOf(user.getId());
            //玩家id
            playerResult.setUserId(userId);
            //房间id
            playerResult.setRoomId(Long.valueOf(roomId));
            //第几局
            playerResult.setGameNum(Integer.valueOf(runingNum));
            //本局得分情况
            playerResult.setScore(scoreMap.get(userIdStr));
            //是否是庄家
            if (Objects.equals(zhuangJiaId, userIdStr)) {
                playerResult.setIsZhuangJia(Boolean.TRUE);
            } else {
                playerResult.setIsZhuangJia(Boolean.FALSE);
            }
            //设置总分
            playerResult.setTotalScore(totalScoreMap.get(userIdStr));
            //设置牌
            playerResult.setPokes(pokesMap.get(userIdStr));
            //设置牌型
            PaiXing paiXing = paiXingMap.get(userIdStr);
            playerResult.setPaiXing(paiXing.getPaixing());
            //设置倍数
            playerResult.setPaiXing(paiXing.getMultiple());
            //设置时间
            playerResult.setEntryTime(entryDatetime);
            playerResults.add(playerResult);
        }
        return playerResults;
    }

}
