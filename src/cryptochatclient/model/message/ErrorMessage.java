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
public class ErrorMessage extends Message {

    private String _errorText;
    
    public ErrorMessage(String errorText){
        super(ERROR);
        _errorText = errorText;
    }

    public String getErrorText() {
        return _errorText;
    }
    
    @Override
    public byte[] getMessageData() throws Exception {
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        target.write(new byte[]{_type});
        target.write(Message.MESSAGE_PART_DELIMITER);
        target.write(_errorText.getBytes("ISO-8859-15"));
        return target.toByteArray();
    }
    
}
