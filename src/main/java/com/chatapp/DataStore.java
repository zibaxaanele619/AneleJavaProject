package com.chatapp;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Saves users and messages to two plain files inside a data folder:
 * users.csv and messages.csv. Text fields are Base64-encoded so commas,
 * tabs and new lines in a message can never break the file format.
 */
public class DataStore {

    private final Path usersFile;
    private final Path messagesFile;

    public DataStore(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create data folder " + directory, e);
        }
        this.usersFile = directory.resolve("users.csv");
        this.messagesFile = directory.resolve("messages.csv");
    }

    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        for (String line : readLines(usersFile)) {
            String[] f = line.split(",", -1);
            if (f.length == 5) {
                users.add(new User(decode(f[0]), decode(f[1]), decode(f[2]), f[3], f[4]));
            }
        }
        return users;
    }

    public void saveUsers(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(String.join(",", encode(u.email()), encode(u.username()), encode(u.userKey()),
                    u.salt(), u.passwordHash()));
        }
        try {
            Files.write(usersFile, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save users", e);
        }
    }

    public List<ChatMessage> loadMessages() {
        List<ChatMessage> messages = new ArrayList<>();
        for (String line : readLines(messagesFile)) {
            String[] f = line.split(",", -1);
            if (f.length != 4) {
                continue;
            }
            try {
                messages.add(new ChatMessage(Long.parseLong(f[0]), decode(f[1]), decode(f[2]), decode(f[3])));
            } catch (NumberFormatException e) {
                // skip a damaged line rather than crash
            }
        }
        return messages;
    }

    public void appendMessage(ChatMessage m) {
        String line = String.join(",", Long.toString(m.timestamp()), encode(m.senderKey()),
                encode(m.senderUsername()), encode(m.text())) + System.lineSeparator();
        try {
            Files.writeString(messagesFile, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save message", e);
        }
    }

    private static List<String> readLines(Path file) {
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
    }

    private static String encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String text) {
        return new String(Base64.getDecoder().decode(text), StandardCharsets.UTF_8);
    }
}
