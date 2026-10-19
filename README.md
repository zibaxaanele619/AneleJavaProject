# Secure Console Chat

Text-based Java chat (Scanner input, no GUI) with accounts and message views.

## Run
    mvn test
    mvn package
    java -jar target/secure-chat-1.0-SNAPSHOT.jar          # data saved in ./data
    java -jar target/secure-chat-1.0-SNAPSHOT.jar mydata   # or choose a data folder

## Commands
    register        create account: email, unique username, password
    login / logout
    whoami          show your username and unique user key
    send <text>     post a message
    displayRecent   latest messages from everyone
    displayOthers   latest messages sent by other users
    displayMine     latest messages you sent

Every message is shown as  [time] username#USERKEY: text

## Notes
- Passwords are stored as salted PBKDF2-HMAC-SHA256 hashes, never as plain text.
- Users and messages are saved in data/users.csv and data/messages.csv (git-ignored).
