package bg.sofia.uni.fmi.mjt.authserver.user;

import bg.sofia.uni.fmi.mjt.authserver.exception.HashException;
import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidCommandException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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

    private final String userId;
    private String username;
    private final String salt;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean admin;

    private static final int SALT_SIZE = 16;
    private static final int SALT_HEX1 = 0xff;
    private static final int SALT_HEX2 = 0x100;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSalt() {
        return salt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public User(String username, String salt, String passwordHash, String userId,
                String firstName, String lastName, String email, boolean admin) {
        this.username = username;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.admin = admin;
    }

    public User(String username, String password, String firstName, String lastName, String email, boolean admin) {
        if (username == null || password == null || firstName == null
                || lastName == null || email == null) {
            throw new InvalidCommandException("All fields are required");
        }
        if (username.contains(",") || password.contains(",") || firstName.contains(",")
                || lastName.contains(",") || email.contains(",")) {
            throw new InvalidCommandException(", is forbidden");
        }
        if (username.contains("\n") || password.contains("\n") || firstName.contains("\n")
                || lastName.contains("\n") || email.contains("\n")) {
            throw new InvalidCommandException("newLine is forbidden");
        }
        this.username = username;
        try {
            this.salt = getNewSalt();
        } catch (NoSuchAlgorithmException e) {
            throw new HashException("There was a problem with the hash algorithm, ", e);
        }
        this.passwordHash = getHashedPassword(password, salt);
        this.userId = UUID.randomUUID().toString();
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
                sb.append(Integer.toString((aByte & SALT_HEX1) + SALT_HEX2, SALT_SIZE)
                        .substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new HashException("problem with hashing password", e);
        }
        return generatedPassword;
    }

    private static String getNewSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[SALT_SIZE];
        sr.nextBytes(salt);
        return new String(salt, StandardCharsets.UTF_8).replaceAll("[" + System.lineSeparator() + ",]", "");
    }

    @Override
    public String toString() {
        return username + "," + salt + "," + passwordHash
                + "," + userId + "," + firstName + ","
                + lastName + "," + email + "," + admin
                + System.lineSeparator();
    }
}
