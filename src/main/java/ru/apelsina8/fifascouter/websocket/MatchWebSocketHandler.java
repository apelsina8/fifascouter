package ru.apelsina8.fifascouter.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.apelsina8.fifascouter.service.GameService;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class MatchWebSocketHandler extends TextWebSocketHandler {

    private final GameService gameService;

    @Autowired
    public MatchWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Получаем URL трансляции от клиента
        String streamUrl = message.getPayload();
        System.out.println("Received stream URL: " + streamUrl);

        // Передаем URL трансляции в GameService для начала обработки
        gameService.start(streamUrl);

        // Сохраняем сессию, чтобы отправлять данные обратно клиенту
        gameService.addSession(session);
    }
}



