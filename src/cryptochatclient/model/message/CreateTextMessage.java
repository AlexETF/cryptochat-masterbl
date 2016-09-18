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
public class CreateTextMessage extends Message {
    
    private String _userSender;
    private String _userRecipient;
    private byte[] _textData;
    
    public CreateTextMessage(String userSender, String userRecipient, byte[] textData){
        super(CREATE_TEXT_MESSAGE);
        _userSender = userSender;
        _userRecipient = userRecipient;
        _textData = textData;
    }

    public String getUserSender() {
        return _userSender;
    }

    public void setUserSender(String _userSender) {
        this._userSender = _userSender;
    }

    public String getUserRecipient() {
        return _userRecipient;
    }

    public void setUserRecipient(String _userRecipient) {
        this._userRecipient = _userRecipient;
    }

    public byte[] getTextData() {
        return _textData;
    }

    public void setTextData(byte[] _textData) {
        this._textData = _textData;
    }
    
    @Override
    public byte[] getMessageData() throws Exception {
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        target.write(new byte[]{_type});
        target.write(Message.MESSAGE_PART_DELIMITER);
        target.write(_userSender.getBytes("ISO-8859-1"));
        target.write(Message.INNER_DELIMITER);
        target.write(_userRecipient.getBytes("ISO-8859-1"));
        target.write(Message.INNER_DELIMITER);
        target.write(_textData);
        
        return target.toByteArray();
    }
    
    
}
