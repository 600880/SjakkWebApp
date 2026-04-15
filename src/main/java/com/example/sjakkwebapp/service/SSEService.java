package com.example.sjakkwebapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

import spill.MoveNotifier;

@Service
public class SSEService implements MoveNotifier {

    private final Map<String, SseEmitter> clients = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String bruker) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        clients.put(bruker, emitter);

        // Send a heartbeat every 20 seconds to keep the connection alive
        new Thread(() -> {
            try {
                while (clients.containsKey(bruker)) {
                    Thread.sleep(20000);
                    emitter.send(SseEmitter.event().name("ping").data("heartbeat"));
                }
            } catch (Exception e) {
                clients.remove(bruker);
            }
        }).start();

        emitter.onCompletion(() -> clients.remove(bruker));
        emitter.onTimeout(() -> clients.remove(bruker));
        emitter.onError((e) -> clients.remove(bruker));

        return emitter;
    }

    public void notifyUser(String bruker, String eventName, String data) {
        SseEmitter emitter = clients.get(bruker);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                clients.remove(bruker);
            }
        }
    }

    @Override
    public void notifyMove(String bruker, String move) {
        notifyUser(bruker, "move", move);
    }

    public void makeAIMove(String bruker, String move) {
        notifyMove(bruker, move);
    }

    public List<String> getOnlineUsers() {
        return new ArrayList<>(clients.keySet());
    }

    public boolean isUserOnline(String bruker) {
        return clients.containsKey(bruker);
    }
}
