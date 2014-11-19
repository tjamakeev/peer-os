package org.safehaus.subutai.core.peer.cli;


import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command(scope = "peer", name = "start-container")
public class StartLxcCommand extends OsgiCommandSupport
{

    private PeerManager peerManager;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Argument(index = 0, name = "hostname", multiValued = false, description = "LXC name")
    private String hostname;


    @Override
    protected Object doExecute() throws Exception
    {

        LocalPeer localPeer = peerManager.getLocalPeer();

        ContainerHost host = localPeer.getContainerHostByName( hostname );

        if ( host == null )
        {
            System.out.println( "Container not found." );
        }
        try
        {
            localPeer.stopContainer( host );
            System.out.println( "Container started successfully" );
        }
        catch ( PeerException e )
        {
            System.out.println( "Could not start container. Error occurred: " + e.toString() );
        }

        return null;
    }
}