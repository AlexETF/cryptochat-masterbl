/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptochatclient.model.message;

import java.io.ByteArrayOutputStream;

/**
 *
 * @author ZM
 */
public class CreateSessionMessage extends Message {
    
    //session for communication with user with _username 
    private String _username;
    //data encrypted with public key
    private byte[] _encryptedSessionData;
    
    public CreateSessionMessage(String username, byte[] encryptedData){
        super(CREATE_SESSION);
        _username = username;
        _encryptedSessionData = encryptedData;
    }

    public String getUsername() {
        return _username;
    }

    public void setUsername(String _username) {
        this._username = _username;
    }

    public byte[] getEncryptedSessionData() {
        return _encryptedSessionData;
    }

    public void setEncryptedSessionData(byte[] _encryptedSessionData) {
        this._encryptedSessionData = _encryptedSessionData;
    }
    
    @Override
    public byte[] getMessageData() throws Exception {
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        target.write(new byte[]{_type});
        target.write(Message.MESSAGE_PART_DELIMITER);
        target.write(_username.getBytes("ISO-8859-1"));
        target.write(Message.INNER_DELIMITER);
        target.write(_encryptedSessionData);
        return target.toByteArray();
    }
}
