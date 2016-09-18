/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptochatclient.model;

import cryptochatclient.controller.Session;
import cryptochatclient.model.message.Message;
import cryptochatclient.crypto.CryptoUtils;
import cryptochatclient.crypto.ICryptoProvider;
import cryptochatclient.model.message.PublicKeyExchangeMessage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import org.bouncycastle.util.encoders.Base64;

/**
 *
 * @author ZM
 */
public class MessageSender {
    
    private Socket _socket;
    private DataOutputStream _out;
    private ICryptoProvider _provider;
    
    public MessageSender(Socket socket, ICryptoProvider provider) throws IOException {
        _socket = socket;
        _out = new DataOutputStream(socket.getOutputStream());
        _provider = provider;
    }
    
    public void send(Message message, Session session) throws Exception {
       byte[] data;
       byte[] encrypted;
       switch (message.getType()) {
            case Message.EXCHANGE_PUBLIC_KEY:
                byte[] key = Base64.encode(((PublicKeyExchangeMessage)message).getKey());
                _out.writeUTF(new String(key));
                break;
            case Message.EXCHANGE_SYMMETRIC_ENCRYPTION_DATA:
                data = Message.getFullMessage(message, session);
                encrypted = Base64.encode(_provider.encryptServerMessage(data));
                _out.writeUTF(new String(encrypted));
                break;
            default:
                data = Message.getFullMessage(message, session);
                encrypted = Base64.encode(CryptoUtils.encryptData(data, session));
                _out.writeUTF(new String(encrypted));
                break;
        }
    }
}
