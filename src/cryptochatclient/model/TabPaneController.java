/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptochatclient.model;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 *
 * @author ZM
 */
public class TabPaneController {
    
    private TabPane _pane;
    
    public TabPaneController(TabPane pane){
        _pane = pane;
    }
    
    public ConversationTab openTabPaneForUser(User user){
        ConversationTab tab = getConversationTab(user);
        if(tab == null){
            tab = new ConversationTab(user);
            _pane.getTabs().add(tab);   
        }
        _pane.getSelectionModel().select(tab);
        
        return tab;
    }
    
    public ConversationTab getConversationTab(User user){
        ConversationTab userTab = null;
        for(Tab tab : _pane.getTabs()){
            if(tab.getText().equals(user.getUsername())){
                userTab = (ConversationTab)tab;
                break;
            }
        }
        return userTab;
    }
    
    public ConversationTab getSelectedTab(){
       return (ConversationTab)_pane.getSelectionModel().getSelectedItem();
    }
}
