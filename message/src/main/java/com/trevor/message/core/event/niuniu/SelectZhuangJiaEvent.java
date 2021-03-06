package com.trevor.message.core.event.niuniu;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.trevor.common.bo.SocketResult;
import com.trevor.common.enums.GameStatusEnum;
import com.trevor.common.util.RandomUtils;
import com.trevor.message.bo.*;
import com.trevor.message.core.event.BaseEvent;
import com.trevor.message.core.event.Event;
import com.trevor.message.core.schedule.CountDownImpl;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 发送庄家事件
 */
@Service
public class SelectZhuangJiaEvent extends BaseEvent implements Event {


    @Override
    public void execute(RoomData roomData, Task task) {
        NiuniuData data = (NiuniuData) roomData;
        String rungingNum = data.getRuningNum();
        String roomId = data.getRoomId();
        Set<String> players = data.getPlayers();
        //防止多次计算庄家
        if (data.getZhuangJiaMap().get(rungingNum) != null) {
            return;
        }
        Set<String> readyPlayers = data.getReadyPlayMap().get(rungingNum);
        data.getQiangZhuangMap().putIfAbsent(rungingNum ,new HashMap<>());
        Map<String, Integer> qiangZhuangMap = data.getQiangZhuangMap().get(rungingNum);
        Map<String, Integer> realQiangZhuangMap = Maps.newHashMap();
        //删除不抢庄的
        for (Map.Entry<String ,Integer> entry : qiangZhuangMap.entrySet()) {
            if (entry.getValue() > 0) {
                realQiangZhuangMap.put(entry.getKey() ,entry.getValue());
            }
        }
        String zhuangJiaUserId;
        List<String> qiangZhuangZhuanQuanList = Lists.newArrayList();
        //没人抢庄
        if (realQiangZhuangMap.isEmpty()) {
            zhuangJiaUserId = noPeopleQiangZhuang(readyPlayers);
            qiangZhuangZhuanQuanList.addAll(readyPlayers);
        } else {
            //一个人抢庄
            if (realQiangZhuangMap.size() == 1) {
                zhuangJiaUserId = onePeopleQiangZhuang(realQiangZhuangMap);
                //多人抢庄
            } else {
                List<Integer> beiShus = Lists.newArrayList();
                for (Integer beiShu : realQiangZhuangMap.values()) {
                    beiShus.add(beiShu);
                }
                //升序排列
                Collections.sort(beiShus);
                List<String> maxBeiShuPlayerIds = Lists.newArrayList();
                Integer maxBeiShu = beiShus.get(beiShus.size()-1);
                for (Map.Entry<String, Integer> entry : realQiangZhuangMap.entrySet()) {
                    if (Objects.equals(entry.getValue(), maxBeiShu)) {
                        maxBeiShuPlayerIds.add(entry.getKey());
                    }
                }
                Integer maxPlayerNum = maxBeiShuPlayerIds.size();
                if (maxPlayerNum == 1) {
                    zhuangJiaUserId = maxBeiShuPlayerIds.get(0);
                } else {
                    Integer randNum = RandomUtils.getRandNumMax(maxPlayerNum);
                    zhuangJiaUserId = maxBeiShuPlayerIds.get(randNum);
                    qiangZhuangZhuanQuanList = maxBeiShuPlayerIds;
                }
            }
        }
        //设置庄家
        data.getZhuangJiaMap().put(rungingNum, zhuangJiaUserId);
        //改变状态
        data.setGameStatus(GameStatusEnum.QIANG_ZHUANG_ZHUAN_QUAN.getCode());


        SocketResult socketResult = new SocketResult(1006, zhuangJiaUserId);
        Boolean noZhuanQuan = qiangZhuangZhuanQuanList.isEmpty();
        if (!noZhuanQuan) {
            socketResult.setZhuanQuanPlayers(qiangZhuangZhuanQuanList);
        }
        socketResult.setGameStatus(GameStatusEnum.QIANG_ZHUANG_ZHUAN_QUAN.getCode());
        socketService.broadcast(roomId, socketResult, players);
        //注册转圈倒计时
        if (!noZhuanQuan) {
            scheduleDispatch.addCountDown(new CountDownImpl(roomId, CountDownNum.THREE, CountDownFlag.ZHUAN_QUAN));
        } else {
            //注册下注倒计时
            scheduleDispatch.addCountDown(new CountDownImpl(roomId, CountDownNum.TWENTY, CountDownFlag.XIA_ZHU));
        }
    }

    /**
     * 没人抢庄
     */
    private String noPeopleQiangZhuang(Set<String> readyPlayers) {
        Integer randNum = RandomUtils.getRandNumMax(readyPlayers.size());
        List<String> playerIds = Lists.newArrayList();
        for (String s : readyPlayers) {
            playerIds.add(s);
        }
        return playerIds.get(randNum);

    }

    /**
     * 一个人抢庄
     *
     * @param qiangZhuangMap
     * @return
     */
    private String onePeopleQiangZhuang(Map<String, Integer> qiangZhuangMap) {
        Iterator<String> iterator = qiangZhuangMap.keySet().iterator();
        while (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

}
