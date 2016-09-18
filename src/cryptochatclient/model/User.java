package cryptochatclient.model;

import cryptochatclient.controller.Session;
import cryptochatclient.crypto.ICryptoProvider;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;

public class User {

    /*
     * 	private fields
     */
    private String _username;
    private PublicKey _publicKey;
    private Session _session;
    private ICryptoProvider _cryptoProvider;
    
    public User(String username) {
        _username = username;
    }

    public User(String username, PublicKey key) {
        this(username);
        _publicKey = key;
    }
    
    public User(String username, ICryptoProvider provider, Session session) {
        this(username);
        _cryptoProvider = provider;
        _session = session;
    }

    public String getUsername() {
        return _username;
    }

    public void setUsername(String username) {
        _username = username;
    }

    public PublicKey getPublicKey() {
        return _publicKey;
    }

    public void setPublicKey(PublicKey _publicKey) {
        this._publicKey = _publicKey;
    }

    public Session getSession() {
        return _session;
    }

    public void setSession(Session _session) {
        this._session = _session;
    }

    
    public ICryptoProvider getCryptoProvider() {
        return _cryptoProvider;
    }

    public void setCryptoProvider(ICryptoProvider _cryptoProvider) {
        this._cryptoProvider = _cryptoProvider;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this._username);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if(this == obj){
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        return this._username.equals(other._username);
    }
    
    @Override
    public String toString(){
        return _username;
    }
   
}
