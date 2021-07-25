package dev.thatismybad.ws.client;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import oshi.SystemInfo;

import java.net.InetAddress;

@Component
public class LoggingTask {
    private static final int INTERVAL = 5000;

    @Value("${ws.address}")
    private String wsAddress;

    private final WebSocketStompClient stompClient;

    public LoggingTask(WebSocketStompClient stompClient) {
        this.stompClient = stompClient;
    }

    @SneakyThrows
    @Scheduled(fixedRate = INTERVAL)
    public void sendMessage() {
        StompSession session = stompClient.connect(wsAddress, new StompSessionHandlerAdapter() {}).get();
        SystemInfo systemInfo = new SystemInfo();
        session.send("/log", new Message(
                InetAddress.getLocalHost().getHostName(), String.format(
                        "{ processCount: %d, ram: %.2f }",
                systemInfo.getOperatingSystem().getProcessCount(),
                (double) systemInfo.getHardware().getMemory().getAvailable() / 1_000_000_000
            )
        ));
    }
}
