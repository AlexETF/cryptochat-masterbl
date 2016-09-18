/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptochatclient.controller;

import cryptochatclient.crypto.CryptoUtils;
import java.time.Instant;
import java.util.Random;

/**
 *
 * @author ZM
 */
public class Session {

    public static final String[] SUPPORTED_HASH_ALGORYTHMS = new String[]{"MD5", "SHA-1", "SHA-256"};
    public static final String[] SUPPORTED_CRYPTO_ALGORYTHMS = new String[]{"AES/CBC/PKCS5Padding", "DES/CBC/PKCS5Padding"};

    public static final byte[] DEFAULT_AES_KEY = new byte[]{1, 1, 1, 1,
        2, 2, 2, 2,
        3, 3, 3, 3,
        4, 4, 4, 4};

    public static final byte[] DEFAULT_AES_IV_VECTOR = new byte[]{1, 1, 1, 1,
        2, 2, 2, 2,
        3, 3, 3, 3,
        4, 4, 4, 4};

    public static final byte[] DEFAULT_DES_KEY = new byte[]{1, 1, 1, 1,
        2, 2, 2, 2};

    public static final byte[] DEFAULT_DES_IV_VECTOR = new byte[]{1, 2, 1, 2,
        1, 2, 1, 2};

    static byte[] generateRandomIvVector(Session serverSession) throws Exception {
        if(SUPPORTED_CRYPTO_ALGORYTHMS[0].equals(serverSession.getSymmetricAlgorythm())){
            return CryptoUtils.generateRandomBytes(16);
        }else if(SUPPORTED_CRYPTO_ALGORYTHMS[1].equals(serverSession.getSymmetricAlgorythm())){
            return CryptoUtils.generateRandomBytes(8);
        }else{
            throw new Exception("Unsupported symmetric algorythm " + serverSession.getSymmetricAlgorythm());
        }
    }

    static byte[] generateRandomKey(Session serverSession) throws Exception {
         if(SUPPORTED_CRYPTO_ALGORYTHMS[0].equals(serverSession.getSymmetricAlgorythm())){
            return CryptoUtils.generateRandomBytes(16);
        }else if(SUPPORTED_CRYPTO_ALGORYTHMS[1].equals(serverSession.getSymmetricAlgorythm())){
            return CryptoUtils.generateRandomBytes(8);
        }else{
            throw new Exception("Unsupported symmetric algorythm " + serverSession.getSymmetricAlgorythm());
        }
    }

    private Instant _utcTime;
    private String _hashAlgorythm;
    private String _symmetricAlgorythm;
    private byte[] _key;
    private byte[] _ivVector;

    public Session() {
        _utcTime = Instant.now();
        _hashAlgorythm = getRandom(SUPPORTED_HASH_ALGORYTHMS);
        _symmetricAlgorythm = getRandom(SUPPORTED_CRYPTO_ALGORYTHMS);
        _key = (_symmetricAlgorythm.equals(SUPPORTED_CRYPTO_ALGORYTHMS[0])) ? DEFAULT_AES_KEY : DEFAULT_DES_KEY;
        _ivVector = (_symmetricAlgorythm.equals(SUPPORTED_CRYPTO_ALGORYTHMS[0])) ? DEFAULT_AES_IV_VECTOR : DEFAULT_DES_IV_VECTOR;
    }

    public Session(String hashAlgorythm, String symmetricAlgorythm) {
        _utcTime = Instant.now();
        _hashAlgorythm = hashAlgorythm;
        _symmetricAlgorythm = symmetricAlgorythm;
        _key = (_symmetricAlgorythm.equals(SUPPORTED_CRYPTO_ALGORYTHMS[0])) ? DEFAULT_AES_KEY : DEFAULT_DES_KEY;
        _ivVector = (_symmetricAlgorythm.equals(SUPPORTED_CRYPTO_ALGORYTHMS[0])) ? DEFAULT_AES_IV_VECTOR : DEFAULT_DES_IV_VECTOR;
    }

    public Session(String symmetricAlgorythm, String hashAlgorythm, byte[] key, byte[] ivVector) {
        _utcTime = Instant.now();
        _hashAlgorythm = hashAlgorythm;
        _symmetricAlgorythm = symmetricAlgorythm;
        _key = key;
        _ivVector = ivVector;
    }

    public Instant getUtcTime() {
        return _utcTime;
    }

    public void setUtcTime(Instant _utcTime) {
        this._utcTime = _utcTime;
    }

    public String getHashAlgorythm() {
        return _hashAlgorythm;
    }

    public String getSymmetricAlgorythm() {
        return _symmetricAlgorythm;
    }

    public void setHashAlgorythm(String _hashAlgorythm) {
        this._hashAlgorythm = _hashAlgorythm;
    }

    public void setSymmetricAlgorythm(String _encodingAlgorythm) {
        this._symmetricAlgorythm = _encodingAlgorythm;
    }

    public byte[] getKey() {
        return _key;
    }

    public void setKey(byte[] _key) {
        this._key = _key;
    }

    public byte[] getIvVector() {
        return _ivVector;
    }

    public void setIvVector(byte[] _ivVector) {
        this._ivVector = _ivVector;
    }

    private String getRandom(String[] items) {
        Random random = new Random();
        return items[random.nextInt(items.length)];
    }

    public static Session getDefaultServerSession() {
        return new Session(SUPPORTED_HASH_ALGORYTHMS[SUPPORTED_HASH_ALGORYTHMS.length - 1],
                SUPPORTED_CRYPTO_ALGORYTHMS[SUPPORTED_CRYPTO_ALGORYTHMS.length - 1]);
    }
}
