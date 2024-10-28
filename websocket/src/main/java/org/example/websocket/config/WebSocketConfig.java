package org.example.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// - @Configuration khai báo lớp này là lớp dùng để cấu hình
// -  @EnableWebSocketMessageBroker dùng để bật các tính năng hỗ trợ giao thức WebSocket
// và sử dụng STOMP (Simple Text Oriented Messaging Protocol) để gửi và nhận tin nhắn giữa các client.
// - WebSocketMessageBrokerConfigurer là 1 inf chứa các phương thức dùng để cấu hình cho WebSocket và STOMP
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

//** Phương thức này dùng để cấu hình broker (máy trung gian) và quy định các tiền tố (prefix) cho điểm đến của tin nhắn.**
// - setApplicationDestinationPrefixes("/app"): xác định tiền tố cho các request từ client gởi đi . vd : /app/message
// khi đó các tin nhắn đó sẽ được xử lý bởi các phương thức trong controller được đánh dấu với @MessageMapping("/message").
// - enableSimpleBroker("/chatroom","/user"); : Kích hoạt simple broker cho các kênh hoặc điểm đến có tiền tố /chatroom và /user.
// Simple broker này sẽ chịu trách nhiệm gửi tin nhắn từ server đến client. Các tin nhắn được gửi tới các điểm đến có
// tiền tố này sẽ được xử lý bởi broker và chuyển tiếp tới các client đã đăng ký lắng nghe.
//  Ví dụ:
// chatroom/public: Được sử dụng cho tin nhắn công khai.
// user/david/private: Được sử dụng cho tin nhắn riêng tư.
// - setUserDestinationPrefix("/user"): Xác định rằng các điểm đến riêng tư (tin nhắn dành cho người dùng cụ thể)
// sẽ bắt đầu bằng tiền tố /user.
// Khi server gửi tin nhắn riêng tư tới một người dùng cụ thể, điểm đến sẽ có dạng /user/{username}/....
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/chatroom","/user");
        registry.setUserDestinationPrefix("/user");
    }


    // ** Phương thức này dùng để đăng ký các STOMP endpoints (điểm kết nối STOMP) mà client sẽ sử dụng để kết nối tới WebSocket server.**
    // - addEndpoint("/ws") : đăng ký điểm kết nối websocket. Muốn kết nối socket thì phải đi qua đường dẫn có /ws
    // - setAllowedOrigins("http://localhost:3000"): chỉ có port 3000 mới được phép truy cập, port khác cook
    // - withSockJS() : Kích hoạt SockJS fallback, hỗ trợ trường hợp trình duyệt không hỗ trợ websocket. Nếu WebSocket không khả dụng
    // thì SockJS sẽ tự động chuyển sang sử dụng các phương thức khác như long polling để duy trì kết nối giữa client và server.
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS();
    }
}
