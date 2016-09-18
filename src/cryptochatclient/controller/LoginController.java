/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptochatclient.controller;


import cryptochatclient.CryptoChatClient;
import cryptochatclient.crypto.CryptoProvider;
import cryptochatclient.crypto.CryptoUtils;
import cryptochatclient.crypto.ICryptoProvider;
import cryptochatclient.model.IGuiNotifier;
import cryptochatclient.model.MessageReceiver;
import cryptochatclient.model.MessageSender;
import cryptochatclient.model.User;
import cryptochatclient.model.message.AckMessage;
import cryptochatclient.model.message.ErrorMessage;
import cryptochatclient.model.message.Message;
import cryptochatclient.model.message.MessageFactory;
import cryptochatclient.model.message.PublicKeyExchangeMessage;
import cryptochatclient.model.message.SymmetricEncryptionDataMessage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class LoginController implements Initializable, IGuiNotifier {

    @FXML
    private ImageView imgLogo;

    @FXML
    private TextField txtPort;

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtIPAdress;

    @FXML
    private Label lblConfigurationError;

    @FXML
    private Label lblServerConnectionError;
    
    @FXML
    private Button btnLogin;
     
    /// Private fields
    private ICryptoProvider _provider;
    private Properties _properties;
    private String _configPath = System.getProperty("user.home") + File.separator + "config.cfg"; 
    
    @FXML
    void btnLoginPressed(ActionEvent event) {
        Platform.runLater(new Runnable(){
            @Override
            public void run(){
                connectToServer(_provider);
            }
        });
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        imgLogo.setImage(new Image(getClass().getResource("/res/logo.png").toExternalForm()));
        lblConfigurationError.setVisible(false);
        lblServerConnectionError.setVisible(false);
        
        _provider = createCryptoProvider();
        loadConnectionProperties();
        
        txtIPAdress.setText(_properties.getProperty("server_ip"));
        txtPort.setText(_properties.getProperty("port"));
        
        txtUsername.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    connectToServer(_provider);
                }
            }
        });
    }
    
    private void loadConnectionProperties(){
        _properties = new Properties();
        try {
            File file = new File(_configPath);
            if (file.exists()) {   
                FileReader read = new FileReader(file);
                _properties.load(read);
                read.close();
            }else {
                _properties.setProperty("server_ip", "localhost");
                _properties.setProperty("port", "9000");
                saveConnectionSettings();
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
    private void saveConnectionSettings() throws IOException{
        File file = new File(_configPath);
        if (file.exists()) {
            file.createNewFile();
        }
        FileWriter write = new FileWriter(file);
        _properties.store(write, null);
        write.close();
    }
    
    private ICryptoProvider createCryptoProvider(){
        try {
            return new CryptoProvider(CryptoUtils.generateRSAKeyPair(2048));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        return null;
    }
    private void connectToServer(ICryptoProvider provider){
        lblConfigurationError.setVisible(false);
        lblServerConnectionError.setVisible(false);
        
        try{
            String userName = txtUsername.getText();
            Session serverSession = new Session();
            if(userName == null || userName.equals("")){
                lblServerConnectionError.setText("ERROR: Enter username");
                lblServerConnectionError.setVisible(true);
                return;
            }
            Integer port = Integer.parseUnsignedInt(txtPort.getText());
            Socket socket = new Socket(txtIPAdress.getText(), port);
            if(socket.isConnected() && sendUserCredentials(socket, userName, provider, serverSession)){
                Stage stage = (Stage)btnLogin.getScene().getWindow();
                stage.hide();
                new CryptoChatClient().start(stage, socket, userName, provider, serverSession);
            }
            
        }catch(NumberFormatException ex){
            lblConfigurationError.setText("ERROR: Connection settings are not correct");
            lblConfigurationError.setVisible(true);
        } catch (IOException ex) {
            lblServerConnectionError.setText("ERROR: Connection failed");
            lblServerConnectionError.setVisible(true);
        } catch (Exception ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public boolean sendUserCredentials(Socket socket, String username, ICryptoProvider iProvider, Session serverSession) throws IOException {
        lblConfigurationError.setVisible(false);
        lblServerConnectionError.setVisible(false);
        try {
            CryptoProvider provider = (CryptoProvider)iProvider;
            
            PublicKeyExchangeMessage message = new PublicKeyExchangeMessage(provider.getPair().getPublic().getEncoded());
            //send genrated 2048 RSA public key
            MessageSender sender = new MessageSender(socket, provider);
            sender.send(message, null);
            
            MessageReceiver receiver = new MessageReceiver(provider, socket, null, new ConcurrentHashMap<String, User>(), this);
            //retrieve server public key
            byte[] data = receiver.readData(MessageReceiver.BUFFER_SIZE);
            if(data.length == 1){
                lblServerConnectionError.setText("Data transfer error, please try again");
                lblServerConnectionError.setVisible(true);
                return false;
            }
            //save server key
            provider.saveServerKey(data);
            
            //generate random key and iv vector for server session
            byte[] key = Session.generateRandomKey(serverSession);
            byte[] ivVector = Session.generateRandomIvVector(serverSession);
            
            serverSession.setKey(key);
            serverSession.setIvVector(ivVector);
            serverSession.setUtcTime(Instant.now());
            
            SymmetricEncryptionDataMessage exchange = new SymmetricEncryptionDataMessage(
                    Instant.now(),
                    username, 
                    serverSession.getSymmetricAlgorythm(),
                    serverSession.getHashAlgorythm(),
                    key,
                    ivVector);
            //send session information
            sender.send(exchange, serverSession);
            
            data = receiver.readData(MessageReceiver.BUFFER_SIZE);
            byte[] decrypted = CryptoUtils.decryptData(data, serverSession);
            
            Message serverFinalAnswer = MessageFactory.getMessage(decrypted, serverSession);
            
            switch(serverFinalAnswer.getType()){
                case Message.ACK:
                    return true;
                case Message.ERROR:
                    ErrorMessage errorAnswer = (ErrorMessage)serverFinalAnswer;
                    lblServerConnectionError.setText(errorAnswer.getErrorText());
                    lblServerConnectionError.setVisible(true);
                    return false;
                default:
                    lblServerConnectionError.setText("ERROR: Unknown message data received from the server");
                    lblServerConnectionError.setVisible(true);
                    return false;
            }
        } catch (Exception ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
        lblServerConnectionError.setText("ERROR: Connection failed");
        lblServerConnectionError.setVisible(true);
        return false;
    }

    @Override
    public void displayMessage(String userID, String message) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void displayUsers(List<String> users) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void userConnected(User username) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void userDisconnected(String username) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void log(String message) {
        lblServerConnectionError.setText("ERROR: Connection failed");
        lblServerConnectionError.setVisible(true);
    }

    @Override
    public void displayStatus(String message) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
