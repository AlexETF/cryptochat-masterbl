/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptochatclient.model;

import java.util.List;

/**
 *
 * @author ZM
 */
public interface IGuiNotifier {
    
    void displayMessage(String userID, String message);
    void displayUsers(List<String> users);
    void userConnected(User username);
    void userDisconnected(String username);
    void log(String message);
    void displayStatus(String message);
}
