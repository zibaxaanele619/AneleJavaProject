package com.chatapp;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/** Registration and login with email + password, a chosen username and a unique user key. */
public class AuthService {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    public static final int MIN_PASSWORD_LENGTH = 8;

    private final DataStore store;
    private final List<User> users;
    private final SecureRandom random = new SecureRandom();

    public AuthService(DataStore store) {
        this.store = store;
        this.users = new ArrayList<>(store.loadUsers());
    }

    /** Creates an account. Throws IllegalArgumentException with a readable reason if something is wrong. */
    public User register(String email, String username, String password) {
        String cleanEmail = email == null ? "" : email.trim().toLowerCase();
        String cleanName = username == null ? "" : username.trim();

        if (!EMAIL.matcher(cleanEmail).matches()) {
            throw new IllegalArgumentException("That doesn't look like a valid email address.");
        }
        if (!USERNAME.matcher(cleanName).matches()) {
            throw new IllegalArgumentException("Username must be 3-20 characters: letters, numbers or underscore.");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        for (User u : users) {
            if (u.email().equals(cleanEmail)) {
                throw new IllegalArgumentException("An account with that email already exists.");
            }
            if (u.username().equalsIgnoreCase(cleanName)) {
                throw new IllegalArgumentException("That username is already taken.");
            }
        }

        String salt = PasswordHasher.newSalt();
        User user = new User(cleanEmail, cleanName, generateUniqueKey(), salt,
                PasswordHasher.hash(password, salt));
        users.add(user);
        store.saveUsers(users);
        return user;
    }

    /** Returns the user if the email and password match, otherwise empty. */
    public Optional<User> login(String email, String password) {
        String cleanEmail = email == null ? "" : email.trim().toLowerCase();
        for (User u : users) {
            if (u.email().equals(cleanEmail)) {
                return PasswordHasher.matches(password, u.salt(), u.passwordHash())
                        ? Optional.of(u) : Optional.empty();
            }
        }
        // Do the same amount of work for unknown emails so response time doesn't reveal which emails exist.
        PasswordHasher.hash(password, PasswordHasher.newSalt());
        return Optional.empty();
    }

    /** An 8-character hex key such as 1A2B3C4D, guaranteed not to be in use already. */
    private String generateUniqueKey() {
        while (true) {
            String key = String.format("%08X", random.nextInt());
            boolean taken = false;
            for (User u : users) {
                if (u.userKey().equals(key)) {
                    taken = true;
                    break;
                }
            }
            if (!taken) {
                return key;
            }
        }
    }
}
