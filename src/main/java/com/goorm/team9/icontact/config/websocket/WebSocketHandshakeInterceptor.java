package com.goorm.team9.icontact.config.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        URI uri = request.getURI();
        MultiValueMap<String, String> params = UriComponentsBuilder.fromUri(uri).build().getQueryParams();

        String roomIdStr = params.getFirst("roomId");
        String senderNickname = params.getFirst("senderNickname");

        if (roomIdStr != null) {
            attributes.put("roomId", Long.parseLong(roomIdStr));
        }
        if (senderNickname != null) {
            attributes.put("senderNickname", senderNickname);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

}
