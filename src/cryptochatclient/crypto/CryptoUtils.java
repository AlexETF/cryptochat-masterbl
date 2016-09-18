/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptochatclient.crypto;

import cryptochatclient.controller.Session;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

/**
 *
 * @author ZM
 */
public class CryptoUtils {

    public static final String RSA_ALGORYTHM = "RSA";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static KeyPair generateRSAKeyPair(int keyLength) throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(RSA_ALGORYTHM, "BC");
        keyGenerator.initialize(keyLength);
        KeyPair keys = keyGenerator.generateKeyPair();
        return keys;
    }

    public static byte[] getHash(byte[] messageData, Session session) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance(session.getHashAlgorythm());
        md.update(messageData);
        return md.digest();
    }

    public static long calculateCRC32(byte[] data) throws UnsupportedEncodingException {
        Checksum checksum = new CRC32();
        checksum.update(data, 0, data.length);
        return checksum.getValue();
    }

    public static byte[] encryptData(byte[] data, Session session) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher c = Cipher.getInstance(session.getSymmetricAlgorythm());
        SecretKeySpec kеySpec = new SecretKeySpec(session.getKey(), session.getSymmetricAlgorythm());
        IvParameterSpec iv = new IvParameterSpec(session.getIvVector());
        c.init(Cipher.ENCRYPT_MODE, kеySpec, iv);
        //byte[] newData = Base64.encode(data);
        return c.doFinal(data);
    }

    public static byte[] decryptData(byte[] data, Session session) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        //byte[] newData = Base64.decode(data);
        Cipher c = Cipher.getInstance(session.getSymmetricAlgorythm());
        SecretKeySpec kеySpec = new SecretKeySpec(session.getKey(), session.getSymmetricAlgorythm());
        IvParameterSpec iv = new IvParameterSpec(session.getIvVector());
        c.init(Cipher.DECRYPT_MODE, kеySpec, iv);
        return c.doFinal(data);
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    public static List<byte[]> split(byte[] input, byte[] delimiter) {
        List<byte[]> byteArrays = new LinkedList<>();
        if (delimiter.length == 0) {
            return byteArrays;
        }
        int begin = 0;

        outer:
        for (int i = 0; i < input.length - delimiter.length + 1; i++) {
            for (int j = 0; j < delimiter.length; j++) {
                if (input[i + j] != delimiter[j]) {
                    continue outer;
                }
            }
            byteArrays.add(Arrays.copyOfRange(input, begin, i));
            begin = i + delimiter.length;
        }
        byteArrays.add(Arrays.copyOfRange(input, begin, input.length));
        return byteArrays;
    }

    public static PublicKey getPublicKeyFromByteArray(byte[] key, String algorythm) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(algorythm);
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(key);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        return publicKey;
    }

    public static byte[] generateRandomBytes(int numberOfBytes) {
        byte[] randomBytes = new byte[numberOfBytes];
        new Random().nextBytes(randomBytes);
        return randomBytes;
    }

    public static byte[] encryptRSA(byte[] data, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(RSA_ALGORYTHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static byte[] decryptRSA(byte[] data, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(RSA_ALGORYTHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }
}
