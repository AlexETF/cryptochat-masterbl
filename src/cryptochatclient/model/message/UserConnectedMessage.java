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
public class UserConnectedMessage extends Message {

    private String _username;
    private byte[] _publicKey;
    
    public UserConnectedMessage(String username, byte[] publicKey){
        super(Message.USER_CONNECTED);
        _username = username;
        _publicKey = publicKey;
    }

    public String getUsername() {
        return _username;
    }

    public void setUsername(String _username) {
        this._username = _username;
    }

    public byte[] getPublicKey() {
        return _publicKey;
    }

    public void setPublicKey(byte[] _publicKey) {
        this._publicKey = _publicKey;
    }
    
    @Override
    public byte[] getMessageData() throws Exception {
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        target.write(new byte[]{_type});
        target.write(Message.MESSAGE_PART_DELIMITER);
        target.write(_username.getBytes("ISO-8859-1"));
        target.write(Message.INNER_DELIMITER);
        target.write(_publicKey);
        return target.toByteArray();
    }
    
}
