/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptochatclient.crypto;

import cryptochatclient.model.User;
import cryptochatclient.model.message.Message;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 *
 * @author ZM
 */
public class CryptoProvider implements ICryptoProvider {

    private KeyPair _pair;
    private PublicKey _serverPublicKey;
    
    public CryptoProvider(KeyPair pair){
        _pair = pair;
    }
    
    @Override
    public KeyPair getPair() {
        return _pair;
    }
    
    @Override
    public byte[] encryptMessage(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, _pair.getPrivate());
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CryptoProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(CryptoProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(CryptoProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(CryptoProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(CryptoProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public byte[] decryptMessage(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, _pair.getPublic());
            
            byte[] decrypted = cipher.doFinal(data);
            System.out.println("Decrypted: " + new String(decrypted));
            return decrypted;
        } catch (InvalidKeyException ex) {
            Logger.getLogger(CryptoProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CryptoProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(CryptoProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(CryptoProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(CryptoProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void saveServerKey(byte[] serverPublicKey) {
        try {
            _serverPublicKey = CryptoUtils.getPublicKeyFromByteArray(serverPublicKey, CryptoUtils.RSA_ALGORYTHM);
        } catch (Exception ex) {
            Logger.getLogger(CryptoProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public byte[] encryptServerMessage(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, _serverPublicKey);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CryptoProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(CryptoProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(CryptoProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(CryptoProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(CryptoProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
