/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptochatclient;

import cryptochatclient.controller.CryptoChatController;
import cryptochatclient.controller.Session;
import cryptochatclient.crypto.ICryptoProvider;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author ZM
 */
public class CryptoChatClient extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/cryptochatclient/view/CryptoChatClient.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setResizable(true);
        stage.setTitle("Crypto Chat");
        stage.setMaximized(true);
        stage.show();
    }

    public void start(Stage stage, Socket socket, String username, ICryptoProvider provider, Session session) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/cryptochatclient/view/CryptoChatClient.fxml"));
        Parent root = fxmlLoader.load();
        CryptoChatController controller = (CryptoChatController) fxmlLoader.getController();

        controller.initializeSocketConnection(socket, username, provider, session);

        Scene scene = new Scene(root);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                controller.closeConnection();
                Platform.exit();
                System.exit(0);
            }
        });
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setTitle("Crypto Chat");
        stage.setMaximized(true);
        stage.show();
    }

}
