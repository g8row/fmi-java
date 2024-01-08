package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.algorithm.Rijndael;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RijndaelTest {
    @Test
    void testEncryptionDecryption() throws IOException, CipherException {
        // Generate a random secret key for testing
        SecretKey secretKey = generateSecretKey();

        // Prepare test data
        String originalData = "This is a test for encryption and decryption.";
        byte[] originalBytes = originalData.getBytes();

        // Encrypt the data
        byte[] encryptedBytes = performEncryption(originalBytes, secretKey);

        // Decrypt the data
        byte[] decryptedBytes = performDecryption(encryptedBytes, secretKey);

        // Verify that the decrypted data matches the original data
        String decryptedData = new String(decryptedBytes);
        assertEquals(originalData, decryptedData);
    }

    @Test
    void testEncryptionDecryptionWithStreams(@TempDir Path tempDir) throws IOException, CipherException {
        // Generate a random secret key for testing
        SecretKey secretKey = generateSecretKey();

        // Prepare test data
        String originalData = "This is a test for encryption and decryption.";
        byte[] originalBytes = originalData.getBytes();

        // Create temporary files for testing
        Path inputFile = tempDir.resolve("input.txt");
        Path encryptedFile = tempDir.resolve("encrypted.txt");
        Path decryptedFile = tempDir.resolve("decrypted.txt");

        // Write original data to the input file
        try (OutputStream outputStream = new FileOutputStream(inputFile.toFile())) {
            outputStream.write(originalBytes);
        }

        // Encrypt the data from the input file and write to the encrypted file
        try (InputStream inputStream = new FileInputStream(inputFile.toFile());
             OutputStream outputStream = new FileOutputStream(encryptedFile.toFile())) {
            Rijndael rijndael = new Rijndael(secretKey);
            rijndael.encrypt(inputStream, outputStream);
        }

        // Decrypt the data from the encrypted file and write to the decrypted file
        try (InputStream inputStream = new FileInputStream(encryptedFile.toFile());
             OutputStream outputStream = new FileOutputStream(decryptedFile.toFile())) {
            Rijndael rijndael = new Rijndael(secretKey);
            rijndael.decrypt(inputStream, outputStream);
        }

        // Read the decrypted data from the decrypted file
        byte[] decryptedBytes = readAllBytes(decryptedFile);

        // Verify that the decrypted data matches the original data
        String decryptedData = new String(decryptedBytes);
        assertEquals(originalData, decryptedData);
    }

    private SecretKey generateSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Error generating secret key", e);
        }
    }

    private byte[] performEncryption(byte[] inputBytes, SecretKey secretKey) throws CipherException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(inputBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Rijndael rijndael = new Rijndael(secretKey);
            rijndael.encrypt(inputStream, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error performing encryption", e);
        }
    }

    private byte[] performDecryption(byte[] inputBytes, SecretKey secretKey) throws CipherException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(inputBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Rijndael rijndael = new Rijndael(secretKey);
            rijndael.decrypt(inputStream, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error performing decryption", e);
        }
    }

    private byte[] readAllBytes(Path file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file.toFile())) {
            return inputStream.readAllBytes();
        }
    }

    @Test
    void testEncryptWithInvalidKey() {
        try (InputStream inputStream = new ByteArrayInputStream("Test".getBytes());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            SecretKey key = null;
            Rijndael rijndael = new Rijndael(key);
            assertThrows(CipherException.class,
                    () -> rijndael.encrypt(inputStream, outputStream));
        } catch (IOException e) {
            throw new RuntimeException("Error in test", e);
        }
    }

    @Test
    void testDecryptWithInvalidKey() {
        try (InputStream inputStream = new ByteArrayInputStream("Test".getBytes());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Rijndael rijndael = new Rijndael(null);
            assertThrows(CipherException.class, () -> rijndael.decrypt(inputStream, outputStream));
        } catch (IOException e) {
            throw new RuntimeException("Error in test", e);
        }
    }
}
