package com.chatapp;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MessageServiceTest {

    @TempDir
    Path dir;

    private MessageService chat;
    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        DataStore store = new DataStore(dir);
        AuthService auth = new AuthService(store);
        alice = auth.register("alice@example.com", "alice", "password123");
        bob = auth.register("bob@example.com", "bob", "password123");
        chat = new MessageService(store);
    }

    @Test
    void sentMessageCarriesUsernameAndKey() {
        ChatMessage m = chat.send(alice, "Hello");
        assertEquals(alice.userKey(), m.senderKey());
        assertTrue(m.format().contains("alice#" + alice.userKey()));
    }

    @Test
    void recentShowsEveryoneNewestFirst() {
        chat.send(alice, "first");
        chat.send(bob, "second");
        List<ChatMessage> recent = chat.getMessages(Category.RECENT, alice);
        assertEquals("second", recent.get(0).text());
        assertEquals("first", recent.get(1).text());
    }

    @Test
    void othersExcludesTheViewersOwnMessages() {
        chat.send(alice, "from alice");
        chat.send(bob, "from bob");
        List<ChatMessage> others = chat.getMessages(Category.OTHERS, alice);
        assertEquals(1, others.size());
        assertEquals("from bob", others.get(0).text());
    }

    @Test
    void mineShowsOnlyTheViewersMessages() {
        chat.send(alice, "from alice");
        chat.send(bob, "from bob");
        List<ChatMessage> mine = chat.getMessages(Category.MINE, alice);
        assertEquals(1, mine.size());
        assertEquals("from alice", mine.get(0).text());
    }

    @Test
    void displayIsLimited() {
        for (int i = 0; i < MessageService.DISPLAY_LIMIT + 5; i++) {
            chat.send(alice, "msg " + i);
        }
        assertEquals(MessageService.DISPLAY_LIMIT, chat.getMessages(Category.RECENT, alice).size());
    }

    @Test
    void emptyAndOversizedMessagesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> chat.send(alice, "   "));
        assertThrows(IllegalArgumentException.class,
                () -> chat.send(alice, "a".repeat(MessageService.MAX_LENGTH + 1)));
    }

    @Test
    void messagesSurviveARestartEvenWithCommasAndAccents() {
        chat.send(alice, "Hi, how are you? Café, ñ, 你好");
        MessageService restarted = new MessageService(new DataStore(dir));
        assertEquals("Hi, how are you? Café, ñ, 你好", restarted.getMessages(Category.RECENT, bob).get(0).text());
    }
}
