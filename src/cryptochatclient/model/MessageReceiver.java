/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptochatclient.model;

import cryptochatclient.controller.Session;
import cryptochatclient.crypto.CryptoUtils;
import cryptochatclient.crypto.ICryptoProvider;
import cryptochatclient.model.message.CreateSessionMessage;
import cryptochatclient.model.message.CreateTextMessage;
import cryptochatclient.model.message.Message;
import cryptochatclient.model.message.MessageFactory;
import static cryptochatclient.model.message.MessageFactory.checkCRC32;
import static cryptochatclient.model.message.MessageFactory.checkHash;
import cryptochatclient.model.message.SymmetricEncryptionDataMessage;
import cryptochatclient.model.message.TextMessage;
import cryptochatclient.model.message.UserConnectedMessage;
import cryptochatclient.model.message.UserDisconnectedMessage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.bouncycastle.util.encoders.Base64;

/**
 *
 * @author ZM
 */
public class MessageReceiver implements Runnable {

    public static final int BUFFER_SIZE = 10000;

    private ICryptoProvider _provider;
    private IGuiNotifier _gui;
    private Socket _socket;
    private boolean _running;
    private Session _serverSession;
    private ConcurrentHashMap<String, User> _tableUsers;

    public MessageReceiver(ICryptoProvider provider, Socket socket, Session session, ConcurrentHashMap<String, User> tableUsers, IGuiNotifier visualizer) {
        _provider = provider;
        _gui = visualizer;
        _socket = socket;
        _running = true;
        _serverSession = session;
        _tableUsers = tableUsers;
    }

    @Override
    public void run() {
        try {
            System.out.println("Started receiver");
            DataInputStream stream = new DataInputStream(_socket.getInputStream());
            List<byte[]> bufferParts = new ArrayList<byte[]>();
            while (_running) {
                try {
                    /*byte[] data = readData(BUFFER_SIZE);
                    ByteArrayOutputStream target = new ByteArrayOutputStream();
                    for (byte[] part : bufferParts) {
                        target.write(part);
                    }
                    byte[] decrypted = CryptoUtils.decryptData(data, _serverSession);
                    target.write(decrypted);
                    bufferParts = CryptoUtils.split(target.toByteArray(), Message.MESSAGE_PART_DELIMITER);
                    List<byte[]> splittedData = getDataAranged(bufferParts);
                    for (byte[] message : splittedData) {
                        processData(message);
                    }*/
                    String data = stream.readUTF();
                    processData(CryptoUtils.decryptData(Base64.decode(data), _serverSession));
                } catch (Exception ex) {
                    _gui.displayStatus(ex.getMessage());
                }
            }
            _gui.log("Disconnected from server");
            _gui.displayStatus("Disconnected");
        } catch (IOException ex) {
            _gui.log("Disconnected from server, due the error");
            Logger.getLogger(MessageReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public byte[] readData(int bufferSize) throws IOException, Exception {
        DataInputStream inStream = new DataInputStream(_socket.getInputStream());
        String data = inStream.readUTF();
        return Base64.decode(data);
    }

    private void processData(byte[] data) throws IOException, Exception {
        System.out.println(_serverSession.getSymmetricAlgorythm());
        System.out.println(_serverSession.getHashAlgorythm());
        System.out.println(_serverSession.getKey());
        System.out.println(_serverSession.getIvVector());
        Message message = MessageFactory.getMessage(data, _serverSession);
        switch (data[0]) {
            case Message.EXCHANGE_PUBLIC_KEY:
                _gui.log("Connected to server");
                _gui.displayStatus("Connected");
                break;
            case Message.CREATE_SESSION:
                CreateSessionMessage sessionMessage = (CreateSessionMessage) message;
                byte[] encrypted = sessionMessage.getEncryptedSessionData();
                byte[] decrypted = CryptoUtils.decryptRSA(encrypted, _provider.getPair().getPrivate());
                message = MessageFactory.getMessage(decrypted, null);
                SymmetricEncryptionDataMessage userSessionMessage = (SymmetricEncryptionDataMessage) message;
                _gui.displayStatus("SESSION REQUEST FROM USER " + userSessionMessage.getUsername());
                if (_tableUsers.containsKey(userSessionMessage.getUsername())) {
                    Session session = new Session(userSessionMessage.getAlgorythm(),
                            userSessionMessage.getHashAlgorythm(),
                            userSessionMessage.getKey(),
                            userSessionMessage.getIvVector());
                    session.setUtcTime(userSessionMessage.getUtcTime());
                    User user = _tableUsers.get(userSessionMessage.getUsername());
                    if (user.getSession() == null) {
                        user.setSession(session);
                        System.out.println("Added session");
                    } else {
                        Instant sessionCreatedTime = user.getSession().getUtcTime();
                        System.out.println("Checking session UTC time");
                        if (sessionCreatedTime.isAfter(session.getUtcTime())) {
                            user.setSession(session);
                            System.out.println("Session replaced");
                        }
                        System.out.println("End of checking session UTC time");
                    }
                } else {
                    _gui.displayStatus("ERROR: Table does not contain user " + userSessionMessage.getUsername());
                }
                break;
            case Message.CREATE_TEXT_MESSAGE:
                CreateTextMessage createTextMessage = (CreateTextMessage)message;
                byte[] encryptedTextData = createTextMessage.getTextData();
                User sender = _tableUsers.get(createTextMessage.getUserSender());
                if(sender == null){
                    return;
                }
                byte[] decryptedTextData = CryptoUtils.decryptData(encryptedTextData,
                        sender.getSession());
                TextMessage textMessage = (TextMessage)MessageFactory.getMessage(decryptedTextData, sender.getSession());
                _gui.displayMessage(sender.getUsername(), textMessage.getText());
                break;
            case Message.USER_CONNECTED:
                UserConnectedMessage connMessage = (UserConnectedMessage) message;
                PublicKey key = CryptoUtils.getPublicKeyFromByteArray(connMessage.getPublicKey(),
                        CryptoUtils.RSA_ALGORYTHM);
                _gui.userConnected(new User(connMessage.getUsername(), key));
                break;
            case Message.USER_DISCONNECTED:
                UserDisconnectedMessage disconnMessage = (UserDisconnectedMessage) message;
                _gui.userDisconnected(disconnMessage.getUsername());
                break;
        }
    }

    private List<byte[]> getDataAranged(List<byte[]> splittedData) throws IOException {
        System.out.println("Arranging data " + splittedData.size());
        List<byte[]> messages = new ArrayList<byte[]>();
        int limit = (splittedData.size() / 4) * 4;
        for (int i = 0; i < limit; i += 4) {
            ByteArrayOutputStream target = new ByteArrayOutputStream();
            target.write(splittedData.get(i));
            System.out.println("Header length " + splittedData.get(i).length);
            for (int j = 1; j < 4 && (i + j) < splittedData.size(); j++) {
                target.write(Message.MESSAGE_PART_DELIMITER);
                target.write(splittedData.get(i + j));
            }
            messages.add(target.toByteArray());
        }
        int rest = splittedData.size() % 4;
        System.out.println("Ostatak je " + rest);
        while (splittedData.size() > rest) {
            System.out.println("Smicem ja ovo polako");
            splittedData.remove(0);
        }
        return messages;
    }

}
