/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptochatclient.model.message;

import cryptochatclient.controller.Session;
import cryptochatclient.crypto.CryptoUtils;
import cryptochatclient.model.User;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author ZM
 */
public class MessageFactory {
    
    public static Message getMessage(byte[] data, Session session) throws Exception{
        switch(data[0]){
            case Message.EXCHANGE_PUBLIC_KEY:
                return createExchangePublicKeyMessage(data, session);
            case Message.EXCHANGE_SYMMETRIC_ENCRYPTION_DATA:
                return creatExchangeSymmetricEncryptionData(data, session);
            case Message.CREATE_SESSION:
                return createSessionMessage(data, session);
            case Message.CREATE_TEXT_MESSAGE:
                return createTextShellMessage(data, session);
            case Message.ACK:
                return createAckMessage(data, session);
            case Message.ERROR:
                return createErrorMessage(data, session);
            case Message.HANDSHAKE:
                return createHandshakeMessage(data, session);
            case Message.CHANGE_PROTOCOL:
                return createChangeProtocolMessage(data, session);
            case Message.TEXT_MESSAGE:
                return createTextMessage(data, session);
            case Message.USER_CONNECTED:
                return createUserConnectedMessage(data, session);
            case Message.USER_DISCONNECTED:
                return createUserDisconnectedMessage(data, session);
            default:
                return createErrorMessage(data, session);
        }
    }

    private static Message createExchangePublicKeyMessage(byte[] data, Session session) throws IOException, Exception {
        List<byte[]> parts = CryptoUtils.split(data, Message.MESSAGE_PART_DELIMITER);
        if(parts.size() != 4){
            throw new Exception("Size of the splitted data is not 4");
        }
        byte[] publicKey = parts.get(1);
        PublicKeyExchangeMessage message = new PublicKeyExchangeMessage(publicKey);
        //check hash value
        if(!checkHash(message, parts.get(2), session)){
            throw new Exception("Invalid hash value, data must be corrupted");
        }
        //check crc32
        if(!checkCRC32(message, parts.get(3))){
            throw new Exception("Invalid crc32 value, data must be corrupted");
        }
        return message;
    }

    private static Message creatExchangeSymmetricEncryptionData(byte[] data, Session session) throws Exception {
        List<byte[]> parts = CryptoUtils.split(data, Message.MESSAGE_PART_DELIMITER);
        if(parts.size() != 4){
            throw new Exception("Size of the splitted data is not 4");
        }
        List<byte[]> innerParts = CryptoUtils.split(parts.get(1), Message.INNER_DELIMITER);
        if(innerParts.size() != 6){
            throw new Exception("Size of the inner splitted data is not 4");
        }
        String username = new String(innerParts.get(0), "ISO-8859-1");
        String cryptoAlgorythm = new String(innerParts.get(1), "ISO-8859-1");
        String hashAlgorythm = new String(innerParts.get(2), "ISO-8859-1");
        
        System.out.println(username);
        System.out.println(cryptoAlgorythm);
        System.out.println(hashAlgorythm);
        
        byte[] key = innerParts.get(3);
        byte[] ivVector = innerParts.get(4);
        long milis = CryptoUtils.bytesToLong(innerParts.get(5));
        
        System.out.println(key.length);
        System.out.println(ivVector.length);
        System.out.println(milis);
        
        SymmetricEncryptionDataMessage message = new SymmetricEncryptionDataMessage(Instant.ofEpochMilli(milis),
                username, cryptoAlgorythm, hashAlgorythm, key, ivVector);
        //check hash value
        Session clientSession = new Session(cryptoAlgorythm, hashAlgorythm, key, ivVector);
        
        if(!checkHash(message, parts.get(2), clientSession)){
            throw new Exception("Invalid hash value, data must be corrupted");
        }
        //check crc32
        if(!checkCRC32(message, parts.get(3))){
            throw new Exception("Invalid crc32 value, data must be corrupted");
        }
        return message;
    }

    
    private static Message createHandshakeMessage(byte[] data, Session session) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static Message createChangeProtocolMessage(byte[] data, Session session) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static Message createTextMessage(byte[] data, Session session) throws Exception {
        List<byte[]> parts = CryptoUtils.split(data, Message.MESSAGE_PART_DELIMITER);
        if(parts.size() != 4){
            throw new Exception("Size of the splitted data is not 4");
        }
        List<byte[]> innerParts = CryptoUtils.split(parts.get(1), Message.INNER_DELIMITER);
        if(innerParts.size() != 1){
            throw new Exception("Size of the inner splitted data is not 2");
        }
        String textData = new String(innerParts.get(0), "ISO-8859-1");
        
        TextMessage message = new TextMessage(textData);
        
        if(!checkHash(message, parts.get(2), session)){
            throw new Exception("Invalid hash value, data must be corrupted");
        }
        //check crc32
        if(!checkCRC32(message, parts.get(3))){
            throw new Exception("Invalid crc32 value, data must be corrupted");
        }
        return message;  
    }

    private static Message createUserConnectedMessage(byte[] data, Session session) throws Exception {
        List<byte[]> parts = CryptoUtils.split(data, Message.MESSAGE_PART_DELIMITER);
        if(parts.size() != 4){
            throw new Exception("Size of the splitted data is not 4");
        }
        List<byte[]> innerParts = CryptoUtils.split(parts.get(1), Message.INNER_DELIMITER);
        if(innerParts.size() != 2){
            throw new Exception("Size of the inner splitted data is not 2");
        }
        String username = new String(innerParts.get(0), "ISO-8859-1");
        byte[] publicKey = innerParts.get(1);
        System.out.println("Inside user connected message");
        System.out.println(username);
        
        UserConnectedMessage message = new UserConnectedMessage(username, publicKey);
        //check hash value
        if(!checkHash(message, parts.get(2), session)){
            throw new Exception("Invalid hash value, data must be corrupted");
        }
        //check crc32
        if(!checkCRC32(message, parts.get(3))){
            throw new Exception("Invalid crc32 value, data must be corrupted");
        }
        return message;
    }

    private static Message createUserDisconnectedMessage(byte[] data, Session session) throws Exception {
        List<byte[]> parts = CryptoUtils.split(data, Message.MESSAGE_PART_DELIMITER);
        if(parts.size() != 4){
            throw new Exception("Size of the splitted data is not 4");
        }
        String username = new String(parts.get(1), "ISO-8859-1");
        System.out.println("Inside user disconnected message");
        System.out.println(username);
        
        UserDisconnectedMessage message = new UserDisconnectedMessage(username);
        //check hash value
        if(!checkHash(message, parts.get(2), session)){
            throw new Exception("Invalid hash value, data must be corrupted");
        }
        //check crc32
        if(!checkCRC32(message, parts.get(3))){
            throw new Exception("Invalid crc32 value, data must be corrupted");
        }
        return message;
    }

    private static Message createAckMessage(byte[] data, Session session) throws Exception {
        List<byte[]> parts = CryptoUtils.split(data, Message.MESSAGE_PART_DELIMITER);
        if(parts.size() != 3){
            throw new Exception("Size of the splitted data is not 4");
        }
        AckMessage message = new AckMessage();
        //check hash value
        if(!checkHash(message, parts.get(1), session)){
            throw new Exception("Invalid hash value, data must be corrupted");
        }
        //check crc32
        if(!checkCRC32(message, parts.get(2))){
            throw new Exception("Invalid crc32 value, data must be corrupted");
        }
        return message;
    }

    private static Message createErrorMessage(byte[] data, Session session) throws Exception {
        List<byte[]> parts = CryptoUtils.split(data, Message.MESSAGE_PART_DELIMITER);
        if(parts.size() != 4){
            throw new Exception("Size of the splitted data is not 4");
        }
        String errorText = new String(parts.get(1), "ISO-8859-1");
        ErrorMessage message = new ErrorMessage(errorText);
        //check hash value
        if(!checkHash(message, parts.get(2), session)){
            throw new Exception("Invalid hash value, data must be corrupted");
        }
        //check crc32
        if(!checkCRC32(message, parts.get(3))){
            throw new Exception("Invalid crc32 value, data must be corrupted");
        }
        return message;
    }

    private static Message createSessionMessage(byte[] data, Session session) throws Exception {
        List<byte[]> parts = CryptoUtils.split(data, Message.MESSAGE_PART_DELIMITER);
        if(parts.size() != 4){
            throw new Exception("Size of the splitted data is not 4");
        }
        List<byte[]> innerParts = CryptoUtils.split(parts.get(1), Message.INNER_DELIMITER);
        if(innerParts.size() != 2){
            throw new Exception("Size of the inner splitted data is not 2");
        }
        String username = new String(innerParts.get(0), "ISO-8859-1");
        byte[] sessionData = innerParts.get(1);
        
        CreateSessionMessage message = new CreateSessionMessage(username, sessionData);
        
        if(!checkHash(message, parts.get(2), session)){
            throw new Exception("Invalid hash value, data must be corrupted");
        }
        //check crc32
        if(!checkCRC32(message, parts.get(3))){
            throw new Exception("Invalid crc32 value, data must be corrupted");
        }
        return message;  
    }
        
    public static boolean checkHash(Message message, byte[] hash, Session session) throws NoSuchAlgorithmException, UnsupportedEncodingException, Exception{
        return Arrays.equals(CryptoUtils.getHash(message.getMessageData(), session), hash);
    }
    
    public static boolean checkCRC32(Message message, byte[] crc32) throws UnsupportedEncodingException, Exception {
        ByteBuffer bb = ByteBuffer.wrap(crc32);
        long lcrc32 = bb.getLong();
        return CryptoUtils.calculateCRC32(message.getMessageData()) == lcrc32;
    }

    private static Message createTextShellMessage(byte[] data, Session session) throws Exception {
        List<byte[]> parts = CryptoUtils.split(data, Message.MESSAGE_PART_DELIMITER);
        if(parts.size() != 4){
            throw new Exception("Size of the splitted data is not 4");
        }
        List<byte[]> innerParts = CryptoUtils.split(parts.get(1), Message.INNER_DELIMITER);
        if(innerParts.size() != 3){
            throw new Exception("Size of the inner splitted data is not 2");
        }
        String userSender = new String(innerParts.get(0), "ISO-8859-1");
        String userRecipient = new String(innerParts.get(1), "ISO-8859-1");
        byte[] textData = innerParts.get(2);
        
        CreateTextMessage message = new CreateTextMessage(userSender, userRecipient, textData);
        
        if(!checkHash(message, parts.get(2), session)){
            throw new Exception("Invalid hash value, data must be corrupted");
        }
        //check crc32
        if(!checkCRC32(message, parts.get(3))){
            throw new Exception("Invalid crc32 value, data must be corrupted");
        }
        return message;  
    }
}
