package io.subutai.common.security.crypto.key;


import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KeyManager
{

    private static final Logger LOG = LoggerFactory.getLogger( KeyManager.class.getName() );

    private KeyPairGenerator keyPairGen;
    private KeyPair keyPair;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private KeyGenerator keyGen;
    private SecretKey secretKey;
    private Signature signature;


    /**
     * *************************************************************************
     * **********************************
     */
    public KeyPairGenerator prepareKeyPairGeneration( KeyPairType keyPairType, int keySize )
    {

        try
        {
            keyPairGen = KeyPairGenerator.getInstance( keyPairType.jce() );
            keyPairGen.initialize( keySize );
        }
        catch ( Exception e )
        {
            LOG.error( "Error preparing KeyPair generator", e );
        }

        return keyPairGen;
    }


    /**
     * *************************************************************************
     * **********************************
     */
    public KeyPair generateKeyPair( KeyPairGenerator keyPairGen )
    {
        try
        {
            keyPair = keyPairGen.genKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        }
        catch ( Exception e )
        {
            LOG.error( "Error generating KeyPair", e );
        }

        return keyPair;
    }


    /**
     * *************************************************************************
     * **********************************
     */
    public PrivateKey generatePrivateKeyPair( KeyPair keyPair )
    {
        try
        {
            privateKey = keyPair.getPrivate();
        }
        catch ( Exception e )
        {
            LOG.error( "Error generating Private Key", e );
        }

        return privateKey;
    }


    /**
     * *************************************************************************
     * **********************************
     */
    public PublicKey generatePublicKeyPair( KeyPair keyPair )
    {
        try
        {
            publicKey = keyPair.getPublic();
        }
        catch ( Exception e )
        {
            LOG.error( "Error Public Key", e );
        }

        return publicKey;
    }


    /**
     * *************************************************************************
     * **********************************
     */
    public byte[] generatePrivateKeyPairBytes()
    {
        byte[] privateKeyBytes = null;

        try
        {
            privateKeyBytes = privateKey.getEncoded();
        }
        catch ( Exception e )
        {
            LOG.error( "Error converting private Key to bytes", e );
        }

        return privateKeyBytes;
    }


    /**
     * *************************************************************************
     * **********************************
     */
    public byte[] generatePublicKeyPairBytes()
    {
        byte[] publicKeyBytes = null;

        try
        {
            publicKeyBytes = publicKey.getEncoded();
        }
        catch ( Exception e )
        {
            LOG.error( "Error converting public Key to bytes", e );
        }

        return publicKeyBytes;
    }


    /**
     * *************************************************************************
     * **********************************
     */
    public void prepareSecretKeyGeneration( SecretKeyType secretKeyType )
    {
        try
        {
            keyGen = KeyGenerator.getInstance( secretKeyType.jce() );
        }
        catch ( NoSuchAlgorithmException e )
        {
            LOG.error( "Error preparing Secret Key generator", e );
        }
    }


    /**
     * *************************************************************************
     * **********************************
     */
    public SecretKey generateSecretKey( KeyGenerator keyGen )
    {
        try
        {
            this.keyGen = keyGen;
            secretKey = this.keyGen.generateKey();
        }
        catch ( Exception e )
        {
            LOG.error( "Error generating Secret Key", e );
        }

        return secretKey;
    }


    /**
     * ** Getter Setter Methods ***********************************************************************
     * **********************************
     */
    public KeyPairGenerator getKeyGen()
    {
        return keyPairGen;
    }


    public void setKeyGen( KeyPairGenerator keyPairGen )
    {
        this.keyPairGen = keyPairGen;
    }


    public PrivateKey getPrivateKey()
    {
        return privateKey;
    }


    public void setPrivateKey( PrivateKey privateKey )
    {
        this.privateKey = privateKey;
    }


    public PublicKey getPublicKey()
    {
        return publicKey;
    }


    public void setPublicKey( PublicKey publicKey )
    {
        this.publicKey = publicKey;
    }


    public KeyPairGenerator getKeyPairGen()
    {
        return keyPairGen;
    }


    public void setKeyPairGen( KeyPairGenerator keyPairGen )
    {
        this.keyPairGen = keyPairGen;
    }


    public KeyPair getKeyPair()
    {
        return keyPair;
    }


    public void setKeyPair( KeyPair keyPair )
    {
        this.keyPair = keyPair;
    }


    public SecretKey getSecretKey()
    {
        return secretKey;
    }


    public void setSecretKey( SecretKey secretKey )
    {
        this.secretKey = secretKey;
    }


    public void setKeyGen( KeyGenerator keyGen )
    {
        this.keyGen = keyGen;
    }


    public Signature getSignature()
    {
        return signature;
    }


    public void setSignature( Signature signature )
    {
        this.signature = signature;
    }
}
