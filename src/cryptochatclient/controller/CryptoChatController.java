/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptochatclient.controller;

import cryptochatclient.crypto.CryptoUtils;
import cryptochatclient.crypto.ICryptoProvider;
import cryptochatclient.model.ConversationTab;
import cryptochatclient.model.IGuiNotifier;
import cryptochatclient.model.MessageReceiver;
import cryptochatclient.model.MessageSender;
import cryptochatclient.model.TabPaneController;
import cryptochatclient.model.User;
import cryptochatclient.model.message.CreateSessionMessage;
import cryptochatclient.model.message.CreateTextMessage;
import cryptochatclient.model.message.Message;
import cryptochatclient.model.message.SymmetricEncryptionDataMessage;
import cryptochatclient.model.message.TextMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URL;
import java.security.KeyPair;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import sun.plugin2.message.Conversation;

public class CryptoChatController implements Initializable {

    @FXML
    private TextArea txtMessage;

    @FXML
    private TreeView<String> treeUsers;

    @FXML
    private Button btnSendMessage;

    @FXML
    private TabPane tabPaneUsers;

    @FXML
    private TextArea txtCommLogs;

    @FXML
    private Label lblStatus;

    @FXML
    private MenuBar menuMainMenu;

    @FXML
    private ImageView imgUser;

    @FXML
    private Label lblConnectedUser;

    // Socket
    private TabPaneController _tabPaneControl;

    private User _user;
    private Socket _socket;
    private MessageReceiver _receiver;
    private MessageSender _sender;
    private IGuiNotifier _notifier;
    private ICryptoProvider _provider;
    private TreeItem<String> _root;
    private ConcurrentHashMap<String, User> _tableUsers;
    
    private final ObservableList<User> _users = FXCollections.observableList(new ArrayList<User>());

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        _root = new TreeItem<String>("Online users");
        _root.setExpanded(true);
        _tableUsers = new ConcurrentHashMap<String, User>();
        _tabPaneControl = new TabPaneController(tabPaneUsers);

        treeUsers.setRoot(_root);
        treeUsers.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    int item = treeUsers.getSelectionModel().getSelectedIndex();
                    if ((item - 1) >= 0 && (item - 1) < _root.getChildren().size()) {
                        User user = _users.get(item - 1);
                        if(user.getSession() == null){
                            try {
                                user.setSession(createSessionForUser(user));
                                sendSessionToUser(user);
                            } catch (Exception ex) {
                                Logger.getLogger(CryptoChatController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        _tabPaneControl.openTabPaneForUser(user);
                    }
                }
            }

            private Session createSessionForUser(User user) throws Exception {
                Session session = new Session();
                session.setKey(Session.generateRandomKey(session));
                session.setIvVector(Session.generateRandomIvVector(session));
                return session;
            }

            private void sendSessionToUser(User user) {
                new Thread(() -> {
                     try {
                        Session session = user.getSession();
                        SymmetricEncryptionDataMessage symMessage = new SymmetricEncryptionDataMessage(
                                Instant.now()
                                , _user.getUsername()
                                , session.getSymmetricAlgorythm()
                                , session.getHashAlgorythm()
                                , session.getKey()
                                , session.getIvVector());
                        
                        byte[] data = Message.getFullMessage(symMessage, session);
                        byte[] encrypted = CryptoUtils.encryptRSA(data, user.getPublicKey());
                        
                        CreateSessionMessage sessionMessage = new CreateSessionMessage(user.getUsername(), encrypted);
                        
                        _sender.send(sessionMessage, _user.getSession());   
                        
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(CryptoChatController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        Logger.getLogger(CryptoChatController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }).start();
            }
        });

        btnSendMessage.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                try {
                    sendMessage();
                } catch (Exception ex) {
                    Logger.getLogger(CryptoChatController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        txtMessage.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    try {
                        sendMessage();
                        keyEvent.consume();
                    } catch (Exception ex) {
                        Logger.getLogger(CryptoChatController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
     
        imgUser.setImage(new Image(getClass().getResource("/cryptochatclient/view/images/user.png").toExternalForm()));
        lblConnectedUser.setText("");
    }

    public void initializeSocketConnection(Socket socket, String username, ICryptoProvider provider, Session session) {
        try {
            _user = new User(username, provider, session);
            _provider = provider;
            _notifier = new MessageArrivedListener();
            _socket = socket;
            _sender = new MessageSender(socket, provider);
            _receiver = new MessageReceiver(provider, socket, session, _tableUsers, _notifier);
            Thread receive = new Thread(_receiver);
            receive.setDaemon(true);
            receive.start();
            lblConnectedUser.setText(_user.getUsername());

        } catch (IOException ex) {
            _notifier.log(ex.getLocalizedMessage());
        }
    }

    public void closeConnection() {
        try {
            if (_socket != null && _socket.isConnected()) {
               // Message message = new Message(Message.USER_DISCONNECTED, _user.getUsername());
                //message.send(_socket.getOutputStream());
                _socket.close();
            }
            if (!_socket.isInputShutdown()) {
                _socket.getInputStream().close();
            }
            if (!_socket.isOutputShutdown()) {
                _socket.getOutputStream().close();
            }
            Platform.exit();
            System.exit(0);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println("Close operation called");
    }

    private User findUser(String username) {
        for (User user : _users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    private void sendMessage() throws Exception {
        if (_socket.isConnected()) {
            try {
                ConversationTab tab = _tabPaneControl.getSelectedTab();
                String messageText = txtMessage.getText();
                if (tab != null && !messageText.equals("")) {
                    User recipient = tab.getUser();
                    System.out.println("Sendint to Recipient " + recipient.getUsername());
                    if(recipient.getSession()== null){
                        System.out.println("Recipient " + recipient.getUsername() + " session is null");
                        return;
                    }
                    Message message = new TextMessage(messageText);
                    byte[] encrypted = CryptoUtils.encryptData(Message.getFullMessage(message, recipient.getSession()), recipient.getSession());
                    message = new CreateTextMessage(
                            _user.getUsername(),
                            recipient.getUsername(),
                            encrypted);

                    tab.appendMyMessage(_user, messageText);
                    txtMessage.clear();
                    _sender.send(message, _user.getSession());
                }
            } catch (IOException ex) {
                _notifier.log("Error while sending message");
                Logger.getLogger(CryptoChatController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /*
     Layer between GUI and Message Receiver subsystem
     */
    class MessageArrivedListener implements IGuiNotifier {

        @Override
        public void displayMessage(String username, String message) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    User user = findUser(username);
                    if (user != null) {
                        ConversationTab tab = _tabPaneControl.openTabPaneForUser(user);
                        tab.appendUserMessage(user, message);
                        log(username + ": " + message + System.lineSeparator());
                    }
                }
            });
        }

        @Override
        public void displayUsers(List<String> users) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    log("List of users arrived, count " + users.size());
                }
            });
        }

        @Override
        public void userConnected(User user) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if(_tableUsers.containsKey(user.getUsername())){
                        return;
                    }
                    _users.add(user);
                    _tableUsers.put(user.getUsername(), user);
                    _root.getChildren().add(new TreeItem<String>(user.getUsername()));
                    _root.setExpanded(true);
                    log(user.getUsername() + " connected ");
                }
            });
        }

        @Override
        public void userDisconnected(String username) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if(!_tableUsers.containsKey(username)){
                        return;
                    }
                    ObservableList<TreeItem<String>> users = _root.getChildren();
                    for (int i = 0; i < _root.getChildren().size(); i++) {
                        if (users.get(i).getValue().equals(username)) {
                            _root.getChildren().remove(i);
                            _users.remove(i);
                            break;
                        }
                    }
                    _root.setExpanded(true);
                    log(username + " disconnected ");
                }
            });
        }

        @Override
        public void log(String message) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    txtCommLogs.appendText(new Date(System.currentTimeMillis()).toString() + " | "
                            + message.replace("\n", "").replace("\r", "") + System.lineSeparator());
                }
            });
        }

        @Override
        public void displayStatus(String message) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    lblStatus.setText(message);
                }
            });
        }

    }

}
