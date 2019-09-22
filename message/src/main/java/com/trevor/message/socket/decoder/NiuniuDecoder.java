package com.trevor.message.socket.decoder;

import com.alibaba.fastjson.JSON;
import com.trevor.message.bo.SocketMessage;

import javax.websocket.EndpointConfig;

/**
 * @author trevor
 * @date 03/30/19 16:01
 */
public class NiuniuDecoder implements javax.websocket.Decoder.Text<SocketMessage> {

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public SocketMessage decode(String str) {
        String a = str;
        return JSON.parseObject(str, SocketMessage.class);
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }
}
