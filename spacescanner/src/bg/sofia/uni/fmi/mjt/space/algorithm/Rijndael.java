package bg.sofia.uni.fmi.mjt.space.algorithm;

import bg.sofia.uni.fmi.mjt.space.exception.CipherException;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Rijndael implements SymmetricBlockCipher {
    private static final String ENCRYPTION_ALGORITHM = "AES"; // //  Advanced Encryption Standard
    SecretKey secretKey;
    public Rijndael(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Encrypts the data from inputStream and puts it into outputStream
     *
     * @param inputStream  the input stream where the data is read from
     * @param outputStream the output stream where the encrypted result is written into
     * @throws CipherException if the encrypt/decrypt operation cannot be completed successfully
     */
    @Override
    public void encrypt(InputStream inputStream, OutputStream outputStream) throws CipherException {
        Cipher cipher = initCipher();
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        } catch (InvalidKeyException e) {
            throw new CipherException("unable to encrypt");
        }
        try (var cipherStream = new CipherOutputStream(outputStream, cipher)) {
            inputStream.transferTo(cipherStream);
        } catch (IOException e) {
            throw new CipherException("unable to encrypt");
        }
    }

    /**
     * Decrypts the data from inputStream and puts it into outputStream
     *
     * @param inputStream  the input stream where the data is read from
     * @param outputStream the output stream where the decrypted result is written into
     * @throws CipherException if the encrypt/decrypt operation cannot be completed successfully
     */
    @Override
    public void decrypt(InputStream inputStream, OutputStream outputStream) throws CipherException {
        Cipher cipher = initCipher();
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
        } catch (InvalidKeyException e) {
            throw new CipherException("unable to decrypt", e);
        }
        try (var cipherStream = new CipherOutputStream(outputStream, cipher)) {
            inputStream.transferTo(cipherStream);
        } catch (IOException e) {
            throw new CipherException("unable to decrypt");
        }
    }

    private Cipher initCipher() {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        return cipher;
    }
}
