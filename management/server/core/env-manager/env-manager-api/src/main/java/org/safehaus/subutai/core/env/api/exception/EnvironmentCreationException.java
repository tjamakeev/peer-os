package org.safehaus.subutai.core.env.api.exception;


public class EnvironmentCreationException extends Exception
{
    public EnvironmentCreationException( final Throwable cause )
    {
        super( cause );
    }


    public EnvironmentCreationException( final String message )
    {
        super( message );
    }
}
