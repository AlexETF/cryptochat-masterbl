/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptochatclient.model.message;

import cryptochatclient.crypto.CryptoUtils;
import java.io.ByteArrayOutputStream;
import java.time.Instant;

/**
 *
 * @author ZM
 */
public class SymmetricEncryptionDataMessage extends Message {
    
    private String _username;
    private String _algorythm;
    private String _hashAlgorythm;
    private byte[] _key;
    private byte[] _ivVector;
    private Instant _utcTime;
     
    public SymmetricEncryptionDataMessage(Instant utcTime, String username, String algorythm, String hashAlgorythm, byte[] key, byte[] ivVector) {
        super(Message.EXCHANGE_SYMMETRIC_ENCRYPTION_DATA);
        _utcTime = utcTime;
        _username = username;
        _algorythm = algorythm;
        _hashAlgorythm = hashAlgorythm;
        _key = key;
        _ivVector = ivVector;
    }

    public String getUsername() {
        return _username;
    }

    public void setUsername(String _username) {
        this._username = _username;
    }

    public String getAlgorythm() {
        return _algorythm;
    }

    public void setAlgorythm(String _algorythm) {
        this._algorythm = _algorythm;
    }

    public String getHashAlgorythm() {
        return _hashAlgorythm;
    }

    public void setHashAlgorythm(String _hashAlgorythm) {
        this._hashAlgorythm = _hashAlgorythm;
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

    public Instant getUtcTime() {
        return _utcTime;
    }

    public void setUtcTime(Instant _utcTime) {
        this._utcTime = _utcTime;
    }

    @Override
    public byte[] getMessageData() throws Exception {
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        target.write(new byte[]{_type});
        target.write(Message.MESSAGE_PART_DELIMITER);
        target.write(_username.getBytes("ISO-8859-1"));
        target.write(Message.INNER_DELIMITER);
        target.write(_algorythm.getBytes("ISO-8859-1"));
        target.write(Message.INNER_DELIMITER);
        target.write(_hashAlgorythm.getBytes("ISO-8859-1"));
        target.write(Message.INNER_DELIMITER);
        target.write(_key);
        target.write(Message.INNER_DELIMITER);
        target.write(_ivVector);
        target.write(Message.INNER_DELIMITER);
        target.write(CryptoUtils.longToBytes(_utcTime.toEpochMilli()));
        return target.toByteArray();
    } 
}
