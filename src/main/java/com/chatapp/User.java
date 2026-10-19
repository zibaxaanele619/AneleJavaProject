package com.chatapp;

/**
 * A registered account.
 * The password itself is never stored, only a salted PBKDF2 hash of it.
 */
public record User(String email, String username, String userKey, String salt, String passwordHash) {
}
