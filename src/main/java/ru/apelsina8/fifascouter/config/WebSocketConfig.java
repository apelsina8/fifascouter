package ru.apelsina8.fifascouter.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import ru.apelsina8.fifascouter.websocket.MatchWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MatchWebSocketHandler matchWebSocketHandler;

    public WebSocketConfig(MatchWebSocketHandler matchWebSocketHandler) {
        this.matchWebSocketHandler = matchWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(matchWebSocketHandler, "/match-data")
                .setAllowedOrigins("localhost:8080"); // Установите свои домены вместо "*" для безопасности
    }
}
