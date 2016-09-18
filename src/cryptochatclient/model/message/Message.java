package cryptochatclient.model.message;

import cryptochatclient.controller.Session;
import cryptochatclient.crypto.CryptoUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public abstract class Message {

    public static final byte[] MESSAGE_START_BYTE = new byte[] { 0x02 };
    public static final byte[] MESSAGE_END_BYTE = new byte[] { 0x03 };
    
    public static final byte[] MESSAGE_PART_DELIMITER = new byte[] {(byte)'|', (byte)'|', (byte)'|', (byte)'|'};
    public static final byte[] INNER_DELIMITER = new byte[] {(byte)'#', (byte)'#'};
    
    public static final byte EXCHANGE_PUBLIC_KEY = 0;
    public static final byte HANDSHAKE = 1;
    public static final byte CHANGE_PROTOCOL = 2;
    public static final byte TEXT_MESSAGE = 3;
    public static final byte USER_CONNECTED = 4;
    public static final byte USER_DISCONNECTED = 5;
    public static final byte EXCHANGE_USERNAME = 6;
    public static final byte EXCHANGE_SYMMETRIC_ENCRYPTION_DATA = 7;
    
    public static final byte CREATE_SESSION = 8;
    public static final byte CREATE_TEXT_MESSAGE = 9;
    
    public static final byte ACK = 101;
    public static final byte CLOSE_CONNECTION = 126;
    public static final byte ERROR = 127;

    /*
     * 	  protected fields
     */
    protected byte _type;

    public Message(byte messageType) {
        _type = messageType;
    }
    
    public byte getType() {
        return _type;
    }

    public abstract byte[] getMessageData() throws Exception;
    
    public boolean checkHash(byte[] hash, Session session) throws NoSuchAlgorithmException, UnsupportedEncodingException, Exception{
        return Arrays.equals(CryptoUtils.getHash(getMessageData(), session), hash);
    }
    
    public static byte[] getFullMessage(Message message, Session session) throws NoSuchAlgorithmException, UnsupportedEncodingException, Exception{
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        target.write(message.getMessageData());
        target.write(Message.MESSAGE_PART_DELIMITER);
        target.write(CryptoUtils.getHash(message.getMessageData(), session));
        target.write(Message.MESSAGE_PART_DELIMITER);
        target.write(CryptoUtils.longToBytes(CryptoUtils.calculateCRC32(message.getMessageData())));
        
        return target.toByteArray();
    }
}
