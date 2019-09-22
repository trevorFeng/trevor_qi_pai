package com.trevor.message.core.event;

import com.trevor.message.bo.RoomData;
import com.trevor.message.bo.Task;

public interface Event {

    void execute(RoomData roomData, Task task);
}
