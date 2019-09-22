package com.trevor.general.feign;

import com.trevor.common.bo.JsonEntity;
import com.trevor.common.domain.mongo.NiuniuRoomParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 一句话描述该类作用:【】
 *
 * @author: trevor
 * @create: 2019-09-21 21:43
 **/

@FeignClient("message")
public interface MessageFeign {

    @RequestMapping(value = "/api/message/create/room/{userId}" ,method = RequestMethod.POST ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    JsonEntity<Long> createRoom(@RequestBody NiuniuRoomParam niuniuRoomParam , @PathVariable("userId") Long userId);

}
