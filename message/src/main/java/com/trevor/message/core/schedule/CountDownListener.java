package com.trevor.message.core.schedule;

public interface CountDownListener {

    /**
     * 事件
     */
    void onCountDown();

    String getRoomId();

}
