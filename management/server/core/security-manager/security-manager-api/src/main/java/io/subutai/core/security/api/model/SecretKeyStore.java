package io.subutai.core.security.api.model;


/**
 * Interface for Secret Keyring store
 */
public interface SecretKeyStore
{

    public String getKeyFingerprint();


    public void setKeyFingerprint( final String keyFingerprint );



    public short getStatus();


    public void setStatus( final short status );


    public short getType();



    public void setType( final short type );



    public String getPwd();



    public void setPwd( final String pwd );



    public byte[] getData();



    public void setData( final byte[] data );

}
