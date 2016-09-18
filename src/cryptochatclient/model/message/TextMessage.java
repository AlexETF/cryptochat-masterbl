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
public class TextMessage extends Message {

    private String _text;
    
    public TextMessage(String text){
        super(TEXT_MESSAGE);
        _text = text;
    }

    public String getText() {
        return _text;
    }
    
    @Override
    public byte[] getMessageData() throws Exception {
         ByteArrayOutputStream target = new ByteArrayOutputStream();
        target.write(new byte[]{_type});
        target.write(Message.MESSAGE_PART_DELIMITER);
        target.write(_text.getBytes("ISO-8859-1"));
        return target.toByteArray();
    }
    
}
