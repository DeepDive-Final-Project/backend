package com.goorm.team9.icontact.config.websocket;

import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        String roomId = request.getURI().getPath().split("/")[2];

        String senderNickname = request.getURI().getQuery();
        String[] params = senderNickname.split("=");
        if (params.length == 2 && "senderNickname".equals(params[0])) {
            senderNickname = params[1];
        }

        attributes.put("roomId", roomId);
        attributes.put("senderNickname", senderNickname);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
