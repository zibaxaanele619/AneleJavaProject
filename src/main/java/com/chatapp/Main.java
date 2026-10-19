package com.chatapp;

import java.io.Console;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/** Text-based chat. All input comes from java.util.Scanner; there is no GUI. */
public class Main {

    public static void main(String[] args) {
        Path dataFolder = Paths.get(args.length > 0 ? args[0] : "data");
        DataStore store = new DataStore(dataFolder);
        run(new Scanner(System.in), new AuthService(store), new MessageService(store));
    }

    static void run(Scanner in, AuthService auth, MessageService chat) {
        User current = null;
        System.out.println("=== Console Chat ===  (type 'help' for commands)");

        while (true) {
            System.out.print(current == null ? "> " : current.username() + "> ");
            if (!in.hasNextLine()) {
                return;
            }
            String line = in.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            String argument = parts.length > 1 ? parts[1] : "";

            try {
                switch (command) {
                    case "help" -> printHelp();
                    case "register" -> current = registerFlow(in, auth);
                    case "login" -> current = loginFlow(in, auth);
                    case "logout" -> {
                        System.out.println(current == null ? "You are not logged in." : "Logged out.");
                        current = null;
                    }
                    case "whoami" -> {
                        User u = requireLogin(current);
                        System.out.println(u.username() + "  (user key: " + u.userKey() + ", " + u.email() + ")");
                    }
                    case "send" -> {
                        chat.send(requireLogin(current), argument);
                        System.out.println("Message sent.");
                    }
                    case "displayrecent" ->
                            show("Recent messages", chat.getMessages(Category.RECENT, requireLogin(current)));
                    case "displayothers" ->
                            show("Messages from others", chat.getMessages(Category.OTHERS, requireLogin(current)));
                    case "displaymine" ->
                            show("Your messages", chat.getMessages(Category.MINE, requireLogin(current)));
                    case "exit", "quit" -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> System.out.println("Unknown command '" + command + "'. Type 'help'.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void printHelp() {
        System.out.println("""
                Commands:
                  register          create an account (email, username, password)
                  login             sign in with email and password
                  logout            sign out
                  whoami            show your username and user key
                  send <text>       post a message
                  displayRecent     the latest messages from everyone
                  displayOthers     the latest messages sent by other users
                  displayMine       the latest messages you sent
                  exit              quit""");
    }

    private static User registerFlow(Scanner in, AuthService auth) {
        String email = ask(in, "Email: ");
        String username = ask(in, "Choose a username: ");
        String password = askPassword(in, "Password (min " + AuthService.MIN_PASSWORD_LENGTH + " chars): ");
        String confirm = askPassword(in, "Confirm password: ");
        if (!password.equals(confirm)) {
            throw new IllegalArgumentException("Passwords did not match.");
        }
        User user = auth.register(email, username, password);
        System.out.println("Welcome, " + user.username() + "! Your unique user key is " + user.userKey() + ".");
        return user;
    }

    private static User loginFlow(Scanner in, AuthService auth) {
        String email = ask(in, "Email: ");
        String password = askPassword(in, "Password: ");
        Optional<User> user = auth.login(email, password);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        System.out.println("Logged in as " + user.get().username() + "#" + user.get().userKey() + ".");
        return user.get();
    }

    private static User requireLogin(User current) {
        if (current == null) {
            throw new IllegalStateException("Please 'login' or 'register' first.");
        }
        return current;
    }

    private static void show(String title, List<ChatMessage> messages) {
        System.out.println("--- " + title + " (newest first) ---");
        if (messages.isEmpty()) {
            System.out.println("(no messages)");
            return;
        }
        for (ChatMessage m : messages) {
            System.out.println(m.format());
        }
    }

    private static String ask(Scanner in, String prompt) {
        System.out.print(prompt);
        return in.hasNextLine() ? in.nextLine().trim() : "";
    }

    /** Hides typing when run in a real terminal; falls back to Scanner inside IDE consoles. */
    private static String askPassword(Scanner in, String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword(prompt);
            return chars == null ? "" : new String(chars);
        }
        System.out.print(prompt);
        return in.hasNextLine() ? in.nextLine() : "";
    }
}
