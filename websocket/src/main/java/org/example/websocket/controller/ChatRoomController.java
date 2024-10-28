package org.example.websocket.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@CrossOrigin("*")
@RequestMapping("")
public class ChatRoomController {

    //  danh sách phòng chat
    private final Set<String> rooms = new HashSet<>();

    // Lấy danh sách phòng chat
    @GetMapping("/rooms")
    public Set<String> getChatRooms() {
        return rooms;
    }

   // Thêm phòng mới vào danh sách
    @PostMapping("/rooms")
    public String createRoom(@RequestBody String roomName) {
        rooms.add(roomName);
        return roomName;
    }
}
