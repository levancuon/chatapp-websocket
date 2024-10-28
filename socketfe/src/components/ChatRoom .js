import React, {useState} from "react";
import SockJS from "sockjs-client";
import {Client} from '@stomp/stompjs';
import '../chatroom.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min';

var stompClient = null;
const ChatRoom = () => {
    const [tab, setTab] = useState("CHATROOM");
    const [publicChats, setPublicChats] = useState([]);
    const [privateChats, setPrivateChats] = useState(new Map());

    // tạo trạng thái ban đầu của các biến
    const [userData, setUserData] = useState({
        username: "",
        receiverName: "",
        connected: false,
        message: ""
    });

    const handleValue = (event) => {
        const {value, name} = event.target;
        setUserData({...userData, [name]: value});
    };

    // - Sử dụng thư viện SockJs để kết nối với server socket
    // - Client là đối tượng stomp được cung cấp bởi thư viện @stomp/stompjs dùng để thiết lập các kết nối đến server với các
    // phương thức onConnect, subcribe, publish ,....
    // - reconnectDelay : thiết lập dùng để tạo khoảng thời gian 5s trước khi thử kết nối lại nếu kết nối bị gián đoạn
    const registerUser = () => {
        let Sock = new SockJS("http://localhost:8080/ws");
        stompClient = new Client({
            webSocketFactory: () => Sock,
            reconnectDelay: 5000,
        });

        stompClient.onConnect = onConnected;
        stompClient.onStompError = onError;
        stompClient.activate();
    };

    // Đăng nhập xong =>  thay đổi các giá trị mặc định của userData
    // Đăng ký cho user thành người chat cộng đồng và người chat cá nhân
    const onConnected = () => {
        setUserData({...userData, "connected": true});
        stompClient.subscribe('/chatroom/public', onPublicMessageReceived);
        stompClient.subscribe(`/user/${userData.username}/private`, onPrivateMessageReceived);
        userJoin();
    };
    // Cấu hình cho user tên và trạng thái
    // Cấu hình đường truyền tin nhắn cho user:
    // + destination là đường dẫn truyền tin nhắn.
    // + body : chuyển thành json để server dễ xử lý
    const userJoin = () => {
        let chatMessage = {
            senderName: userData.username,
            status: "JOIN"
        };
        stompClient.publish({destination: '/app/message', body: JSON.stringify(chatMessage)});
    };
    const onError = (err) => {
        console.log(err);
    };

    const onPublicMessageReceived = (payload) => {
        let payloadData = JSON.parse(payload.body);
        switch (payloadData.status) {
            case "JOIN":
                if (!privateChats.get(payloadData.senderName)) {
                    privateChats.set(payloadData.senderName, []);
                    setPrivateChats(new Map(privateChats));
                }
                break;
            case "MESSAGE":
                publicChats.push(payloadData);
                setPublicChats([...publicChats]);
                break;
            default:
                break;
        }
    };

    const onPrivateMessageReceived = (payload) => {
        let payloadData = JSON.parse(payload.body);
        if (privateChats.get(payloadData.senderName)) {
            privateChats.get(payloadData.senderName).push(payloadData);
            setPrivateChats(new Map(privateChats));
        } else {
            let list = [];
            list.push(payloadData);
            privateChats.set(payloadData.senderName, list);
            setPrivateChats(new Map(privateChats));
        }
    };


    const sendPublicMessage = () => {
        if (stompClient) {
            let chatMessage = {
                senderName: userData.username,
                message: userData.message,
                status: "MESSAGE"
            };
            stompClient.publish({destination: '/app/message', body: JSON.stringify(chatMessage)});
            setUserData({...userData, "message": ""});
        }
    };

    const sendPrivateMessage = () => {
        if (stompClient) {
            let chatMessage = {
                senderName: userData.username,
                receiverName: tab,
                message: userData.message,
                status: "MESSAGE"
            };
            if (userData.username !== tab) {
                privateChats.get(tab).push(chatMessage);
                setPrivateChats(new Map(privateChats));
            }
            stompClient.publish({destination: '/app/private-message', body: JSON.stringify(chatMessage)});
            setUserData({...userData, "message": ""});
        }
    };

    return (
        <div className="container">
            {userData.connected ? (
                <div className="chat-box">
                    <div className="member-list">
                        <div>
                            <button onClick={() => setTab("CHATROOM")}
                                    className={`member ${tab === "CHATROOM" && "active"}`}>
                                Chatroom
                            </button>
                        </div>
                        {[...privateChats.keys()].map((name, index) => (
                            <div key={index}>
                                <button onClick={() => setTab(name)} className={`member ${tab === name && "active"}`}>
                                    {name}
                                </button>
                            </div>
                        ))}
                    </div>

                    <div className="chat-content">
                        <div className="chat-message">
                            {(tab === "CHATROOM" ? publicChats : privateChats.get(tab) || []).map((chat, index) => (
                                <div
                                    key={index}
                                    className={`message ${
                                        chat.senderName === userData.username ? "message-right" : "message-left"
                                    }`}
                                >
                                    {chat.senderName !== userData.username && (
                                        <div className="avatar">{chat.senderName}</div>
                                    )}
                                    <div
                                        className={`message-data ${chat.senderName === userData.username ? "self" : ""}`}>
                                        {chat.message}
                                    </div>
                                    {chat.senderName === userData.username && (
                                        <div className="avatar self">{chat.senderName}</div>
                                    )}
                                </div>
                            ))}
                        </div>
                        <div className="send-message">
                            <input
                                type="text"
                                className="input-message"
                                name="message"
                                placeholder={`Enter your message`}
                                value={userData.message}
                                onChange={handleValue}
                            />
                            <button
                                type="button"
                                className="send-button"
                                onClick={tab === "CHATROOM" ? sendPublicMessage : sendPrivateMessage}
                            >
                                Send
                            </button>
                        </div>
                    </div>
                </div>
            ) : (
                <div className="register">
                    <input
                        id="user-name"
                        name="username"
                        placeholder="Enter the username"
                        value={userData.username}
                        onChange={handleValue}
                    />
                    <button type="button" onClick={registerUser}>
                        Connect
                    </button>
                </div>
            )}
        </div>
    );
};

export default ChatRoom;
