package org.example.websocket.controller;

import org.example.websocket.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    // SimpMessagingTemplate cho phép gởi nôi dung từ server đến client thông qua websocket
    // Cho phép gởi tin nhắn đến các điểm cuối (endpoints) khác nhau,
    // ví dụ như các kênh chat công khai (public) hoặc người dùng cụ thể (private).
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    //Quy trình:
    //1 : Client gửi tin nhắn tới điểm /app/message.
    //2 : Tin nhắn này được xử lý bởi phương thức receivePublicMessage.
    //3 : Tin nhắn sau đó được gửi tới tất cả client đang lắng nghe tại điểm /chatroom/public.
    // @MessageMapping : Xử lý các tin nhắn gởi đến địa chỉ /message
    // @SendTo : tin nhắn server nhận được sẽ gởi đến mọi client đang có đường dẫn nhận tin là /chatroom/public
    // @Payload Message message : Payload của thông điệp sẽ được ánh xạ vào đối tượng Message (giả sử Message là một
    // class đại diện cho tin nhắn trong ứng dụng).
    @MessageMapping("/message")
    @SendTo("/chatroom/public")
    public Message receivePublicMessage(@Payload Message message) {
        return message;
    }


    // simpMessagingTemplate.convertAndSendToUser : Có 3 tham số
    // - Chỉ định người nhận
    // - Địa chỉ người nhận
    // - nội dung được gởi
    // Quy trình:
    // 1 : Client gửi tin nhắn tới điểm /app/private-message.
    // 2 : Tin nhắn này được xử lý bởi phương thức receivePrivateMessage.
    // 3 : Server sử dụng simpMessagingTemplate để gửi tin nhắn tới người dùng cụ thể bằng tên, tại điểm /user/{receiverName}/private.
    // 4 : Người nhận cụ thể sẽ nhận được tin nhắn riêng tư này.
   @MessageMapping("/private-message")
    public Message receivePrivateMessage(@Payload Message message){
        simpMessagingTemplate.convertAndSendToUser(message.getReceiverName(),"/private", message); // /user/david/private
        return message;
   }
}