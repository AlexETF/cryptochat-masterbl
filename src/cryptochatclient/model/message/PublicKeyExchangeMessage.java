/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptochatclient.model.message;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ZM
 */
public class PublicKeyExchangeMessage extends Message {

    private byte[] _key;
    
    public PublicKeyExchangeMessage(byte[] data) {
        super(Message.EXCHANGE_PUBLIC_KEY);
        _key = data;
    }

    public byte[] getKey() {
        return _key;
    }
    
    @Override
    public byte[] getMessageData() throws Exception {
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        target.write(new byte[]{_type});
        target.write(Message.MESSAGE_PART_DELIMITER);
        target.write(_key);
        return target.toByteArray();
    }  
}
