/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptochatclient.model;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;

/**
 *
 * @author ZM
 */
public class ConversationTab extends Tab {
    
    private static StringBuilder htmlHeader;
    private static StringBuilder htmlFooter;
    private static StringBuilder cssStyle;
    private static String backgroundColor = "-fx-background-color: #3498db";
    
    static {
        //load css 
        try {
            htmlHeader = new StringBuilder();
            htmlFooter = new StringBuilder();
            cssStyle = new StringBuilder();
            BufferedReader stream = new BufferedReader(new InputStreamReader((
                    ConversationTab.class.getResourceAsStream("/cryptochatclient/view/css/bubbles.css"))));
            String line = "";
            while((line = stream.readLine()) != null){
                cssStyle.append(line);
            }
            htmlHeader.append("<head>");  
            htmlHeader.append("   <script language=\"javascript\" type=\"text/javascript\">");  
            htmlHeader.append("       function toBottom(){");  
            htmlHeader.append("           window.scrollTo(0, document.body.scrollHeight);");  
            htmlHeader.append("       }");  
            htmlHeader.append("   </script>");
            htmlHeader.append("   <style> ");
            htmlHeader.append(cssStyle.toString());
            htmlHeader.append("   </style>");
            htmlHeader.append("</head>");
            htmlHeader.append("<body style='background-color:#e4eef2' onload='toBottom()'>");
            htmlHeader.append("<section><div class='chat'><ul>");
            htmlFooter.append("</ul></div></section></body></html>");
        } catch (IOException ex) {
            Logger.getLogger(ConversationTab.class.getName()).log(Level.SEVERE, null, ex);
        }catch(Exception ex){
            Logger.getLogger(ConversationTab.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private User _user;
    private WebView _messages = new WebView();
    private StringBuilder _text;
    
    public ConversationTab(User user){
        _user = user;
        initComponents();
    }
    
    private void initComponents(){
        _text = new StringBuilder();
        _messages.setContextMenuEnabled(false);
        _messages.getEngine().loadContent(htmlHeader.toString() + htmlFooter.toString());
        BorderPane pane = new BorderPane();
        pane.setStyle(backgroundColor);
        pane.setPadding(new Insets(10, 10, 10, 10));
        pane.setCenter(_messages);
        setText(_user.getUsername());
        setContent(pane);
        setClosable(true);
    }
    
    public void appendMyMessage(User user, String message){
        StringBuilder current = new StringBuilder();
        current.append(htmlHeader);
        _text.append("<li class=\"other\">");
        _text.append("<a class='user' href='#'><img alt='' "
                + "src='https://s3.amazonaws.com/uifaces/faces/twitter/igorgarybaldi/128.jpg'/></a>");
        _text.append("<div class='date'>" + new Date().toString() + "</div>");
        _text.append("<div class=\"message\"><p>" + message);
        _text.append("</p></div></li></br>" + System.lineSeparator());
        current.append(_text);
        current.append(htmlFooter);
        try{
            _messages.getEngine().loadContent(current.toString());
        }catch(NullPointerException e){
            System.out.println("Scroll Nullpointer exception caugth");
        }
    }
    
    public void appendUserMessage(User user, String message){
        StringBuilder current = new StringBuilder();
        current.append(htmlHeader);
        _text.append("<li class=\"you\">");
        _text.append("<a class='user' href='#'><img alt='' "
                + "src='https://s3.amazonaws.com/uifaces/faces/twitter/toffeenutdesign/128.jpg'/></a>");
        _text.append("<div class='date'>" + new Date().toString() + "</div>");
        _text.append("<div class=\"message\"><p>" + message);
        _text.append("</p></div></li></br>" + System.lineSeparator());
        current.append(_text);
        current.append(htmlFooter);
        try{
            _messages.getEngine().loadContent(current.toString());
        }catch(NullPointerException e){
            System.out.println("Scroll Nullpointer exception caugth");
        }
    }

    public User getUser() {
        return _user;
    }
    
}
