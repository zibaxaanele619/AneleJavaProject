package com.chatapp;

import java.util.ArrayList;
import java.util.List;

/** Sends messages and returns them grouped by Category. */
public class MessageService {

    public static final int MAX_LENGTH = 500;
    public static final int DISPLAY_LIMIT = 10;

    private final DataStore store;
    private final List<ChatMessage> messages;

    public MessageService(DataStore store) {
        this.store = store;
        this.messages = new ArrayList<>(store.loadMessages());
    }

    public ChatMessage send(User sender, String text) {
        String clean = text == null ? "" : text.trim();
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("Type a message after 'send', e.g.  send Hello everyone");
        }
        if (clean.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Messages can be at most " + MAX_LENGTH + " characters.");
        }
        ChatMessage message = new ChatMessage(System.currentTimeMillis(), sender.userKey(),
                sender.username(), clean);
        messages.add(message);
        store.appendMessage(message);
        return message;
    }

    /** Newest first, at most DISPLAY_LIMIT messages. */
    public List<ChatMessage> getMessages(Category category, User viewer) {
        List<ChatMessage> result = new ArrayList<>();
        for (int i = messages.size() - 1; i >= 0 && result.size() < DISPLAY_LIMIT; i--) {
            ChatMessage m = messages.get(i);
            boolean fromViewer = m.senderKey().equals(viewer.userKey());
            boolean include = switch (category) {
                case RECENT -> true;
                case OTHERS -> !fromViewer;
                case MINE -> fromViewer;
            };
            if (include) {
                result.add(m);
            }
        }
        return result;
    }
}
