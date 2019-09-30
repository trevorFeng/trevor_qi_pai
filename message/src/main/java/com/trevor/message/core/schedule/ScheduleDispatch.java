package com.trevor.message.core.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 计时器总调度
 */
@Service
public class ScheduleDispatch {


    private ConcurrentHashMap<String, CountDownListener> listeners = new ConcurrentHashMap<>(2 << 7);

    /**
     * 添加事件
     *
     * @param listener
     */
    public void addCountDown(CountDownListener listener) {
        if (!listeners.containsKey(listener)) {
            listeners.put(listener.getRoomId(), listener);
        }
    }

    /**
     * 移除事件,key为房间id
     *
     * @param key
     */
    public void removeCountDown(String key) {
        listeners.remove(key);
    }

    /**
     * 移除事件
     *
     * @param listener
     */
    public void removeCountDown(CountDownListener listener) {
        removeCountDown(listener.getRoomId());
    }

    /**
     * 每一秒执行一次
     */
    @Scheduled(cron = "*/1 * * * * ?")
    public void loopRedPacketBySec() {
        //如果移除监听器时，如果变化发生在已经遍历的桶，则迭代过程不会再感知这个变化，直接删除调即可，移除发生在未遍历的segement，则本次迭代会感知到这个元素。
        Iterator<CountDownListener> iterator = listeners.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().onCountDown();
        }
    }

//    /**
//     * 超过12小时未使用的房间会被自动关闭
//     */
//    @Scheduled(initialDelay = 1000 * 60 * 60 ,fixedRate = 5000 * 60 * 30)
//    public void checkRoom(){
//        log.info("检查房间开始");
//        //房间半小时内未使用会被关闭
//        try {
//            taskService.checkRoomRecord();
//        }catch (Exception e) {
//            e.printStackTrace();
//            log.error(e.toString());
//        }
//    }

}
