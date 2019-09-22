package com.trevor.message.web.controller;

import com.trevor.common.bo.JsonEntity;
import com.trevor.common.domain.mongo.NiuniuRoomParam;
import com.trevor.message.service.CreateRoomService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
public class CreateRoomController {

    @Resource
    private CreateRoomService createRoomService;

    @RequestMapping(value = "/api/message/create/room/{userId}", method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public JsonEntity<Long> createRoom(@PathVariable("userId") String userId, @RequestBody NiuniuRoomParam niuniuRoomParam) {
        return createRoomService.createRoom(niuniuRoomParam, userId);
    }
}
