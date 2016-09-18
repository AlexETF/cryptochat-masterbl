/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import cryptochatclient.controller.Session;
import cryptochatclient.crypto.CryptoProvider;
import cryptochatclient.crypto.CryptoUtils;
import cryptochatclient.model.message.Message;
import cryptochatclient.model.message.PublicKeyExchangeMessage;
import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.bouncycastle.util.encoders.Base64;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ZM
 */
public class CryptoTests {
    
    public CryptoTests() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    /*@Test
    public void TestDecyphering() throws Exception {
        
        KeyPair pair = CryptoUtils.generateRSAKeyPair(512);
        CryptoProvider provider = new CryptoProvider(pair);
        
        PublicKeyExchangeMessage message = new PublicKeyExchangeMessage(provider.getPair().getPublic().getEncoded());
        
        Session session = Session.getDefaultServerSession();  
        byte[] text = Message.getFullMessage(message, session);
        
        byte[] encrypted = CryptoUtils.encryptData(text, session);
        byte[] decrypted = CryptoUtils.decryptData(encrypted, session);
        
        System.out.println(text.length);
        String decodedText = new String(decrypted);
        
        assert text.length== decrypted.length : "Not equal in count";
        assert Arrays.equals(text, decrypted) : "Original message and decrypted data are not equal";
        
    }*/
    
    @Test
    public void TestString() throws Exception {
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        KeyPair pair = CryptoUtils.generateRSAKeyPair(512);
        String name = "Aleksandar";
        target.write(Base64.encode(name.getBytes()));
        String keyToString = new String(Base64.encode(pair.getPublic().getEncoded()), "UTF-8");
        //keyToString = new String(keyToString.getBytes(), "UTF-8");
        assert Arrays.equals(Base64.decode(keyToString), pair.getPublic().getEncoded()) : "Not equal";
    }
}
