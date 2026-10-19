package com.chatapp;

/** The three ways messages can be viewed. */
public enum Category {
    /** The latest messages from everyone (displayRecent). */
    RECENT,
    /** Messages sent by other users (displayOthers). */
    OTHERS,
    /** Messages the logged-in user sent (displayMine). */
    MINE
}
