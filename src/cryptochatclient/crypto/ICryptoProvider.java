/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptochatclient.crypto;

import cryptochatclient.model.message.Message;
import java.io.OutputStream;
import java.security.KeyPair;

/**
 *
 * @author ZM
 */
public interface ICryptoProvider {
    
    void saveServerKey(byte[] serverPublicKey);
    KeyPair getPair();
    byte[] encryptServerMessage(byte[] message);
    byte[] encryptMessage(byte[] message);
    byte[] decryptMessage(byte[] message);
    
}
