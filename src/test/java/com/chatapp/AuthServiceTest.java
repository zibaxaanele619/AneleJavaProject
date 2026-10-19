package com.chatapp;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AuthServiceTest {

    @TempDir
    Path dir;

    private AuthService auth;

    @BeforeEach
    void setUp() {
        auth = new AuthService(new DataStore(dir));
    }

    @Test
    void registerAssignsKeyAndUsername() {
        User u = auth.register("alice@example.com", "alice", "password123");
        assertEquals("alice", u.username());
        assertEquals(8, u.userKey().length());
    }

    @Test
    void userKeysAreUnique() {
        User a = auth.register("a@example.com", "userA", "password123");
        User b = auth.register("b@example.com", "userB", "password123");
        assertNotEquals(a.userKey(), b.userKey());
    }

    @Test
    void duplicateEmailIsRejected() {
        auth.register("alice@example.com", "alice", "password123");
        assertThrows(IllegalArgumentException.class,
                () -> auth.register("ALICE@example.com", "other", "password123"));
    }

    @Test
    void duplicateUsernameIsRejectedIgnoringCase() {
        auth.register("alice@example.com", "alice", "password123");
        assertThrows(IllegalArgumentException.class,
                () -> auth.register("b@example.com", "ALICE", "password123"));
    }

    @Test
    void invalidInputIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> auth.register("not-an-email", "alice", "password123"));
        assertThrows(IllegalArgumentException.class, () -> auth.register("a@example.com", "x", "password123"));
        assertThrows(IllegalArgumentException.class, () -> auth.register("a@example.com", "alice", "short"));
    }

    @Test
    void loginWorksWithCorrectPasswordOnly() {
        auth.register("alice@example.com", "alice", "password123");
        assertTrue(auth.login("alice@example.com", "password123").isPresent());
        assertTrue(auth.login("alice@example.com", "wrongpass1").isEmpty());
        assertTrue(auth.login("nobody@example.com", "password123").isEmpty());
    }

    @Test
    void accountsSurviveARestart() {
        User original = auth.register("alice@example.com", "alice", "password123");
        AuthService restarted = new AuthService(new DataStore(dir));
        assertEquals(original.userKey(), restarted.login("alice@example.com", "password123").orElseThrow().userKey());
    }

    @Test
    void passwordIsNotStoredAsPlainText() throws IOException {
        auth.register("alice@example.com", "alice", "password123");
        String file = Files.readString(dir.resolve("users.csv"));
        assertFalse(file.contains("password123"));
    }
}
