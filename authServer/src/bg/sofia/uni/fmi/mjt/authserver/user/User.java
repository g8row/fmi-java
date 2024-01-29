package bg.sofia.uni.fmi.mjt.authserver.user;

import bg.sofia.uni.fmi.mjt.authserver.exception.HashException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

public class User {
    public static final int USERNAME_POS = 0;
    public static final int SALT_POS = 1;
    public static final int PASS_POS = 2;
    public static final int ID_POS = 3;
    public static final int FNAME_POS = 4;
    public static final int LNAME_POS = 5;
    public static final int EMAIL_POS = 6;
    public static final int ADMIN_POS = 7;

    public static final List<String> FIELDS = List.of(
            new String[]{"--username", "--password", "--first-name", "--last-name", "--email"});
    private String userId;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private boolean admin;

    private static final int SALT_SIZE = 16;
    private static final int HEX1 = 0xff;
    private static final int HEX2 = 0x100;

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public User setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public User setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isAdmin() {
        return admin;
    }

    public User() {
        userId = UUID.randomUUID().toString();
        username = null;
        password = null;
        firstName = null;
        lastName = null;
        email = null;
        admin = false;
    }

    public User(String username, String password, String firstName, String lastName, String email, boolean admin) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.admin = admin;
    }

    public User(String username, String password, String userId,
                String firstName, String lastName, String email, boolean admin) {
        this.username = username;
        this.password = password;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.admin = admin;
    }

    public static String getHashedPassword(String passwordToHash,
                                           String salt) {
        String generatedPassword;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(passwordToHash.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & HEX1) + HEX2, SALT_SIZE)
                        .substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new HashException("problem with hashing password", e);
        }
        return generatedPassword;
    }

    private static String getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[SALT_SIZE];
        sr.nextBytes(salt);
        return new String(salt, StandardCharsets.UTF_8).replaceAll(System.lineSeparator(), "");
    }

    @Override
    public String toString() {
        String salt;
        try {
            salt = getSalt();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return username + "," + salt + "," + getHashedPassword(password, salt)
                + "," + userId + "," + firstName + ","
                + lastName + "," + email + "," + admin
                + System.lineSeparator();
    }

    public static User of(String str) {
        String[] split = str.split(",");
        return new User(split[USERNAME_POS], split[PASS_POS], split[ID_POS], split[FNAME_POS],
                split[LNAME_POS], split[EMAIL_POS], Boolean.parseBoolean(split[ADMIN_POS]));
    }
}
