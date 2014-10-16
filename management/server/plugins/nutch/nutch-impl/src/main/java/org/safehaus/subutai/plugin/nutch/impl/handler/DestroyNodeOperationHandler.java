package org.safehaus.subutai.plugin.nutch.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.nutch.api.NutchConfig;
import org.safehaus.subutai.plugin.nutch.api.SetupType;
import org.safehaus.subutai.plugin.nutch.impl.NutchImpl;

import com.google.common.collect.Sets;


public class DestroyNodeOperationHandler extends AbstractOperationHandler<NutchImpl>
{
    private final String lxcHostname;


    public DestroyNodeOperationHandler( NutchImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( NutchConfig.PRODUCT_KEY,
                String.format( "Destroying %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public void run()
    {
        ProductOperation po = productOperation;
        NutchConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null )
        {
            po.addLogFailed(
                    String.format( "Agent with hostname %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }

        if ( config.getNodes().size() == 1 )
        {
            po.addLogFailed(
                    "This is the last slave node in the cluster. Please, destroy cluster instead\nOperation aborted" );
            return;
        }

        //check if node is in the cluster
        if ( !config.getNodes().contains( agent ) )
        {
            po.addLogFailed( String.format( "Node %s does not belong to this cluster\nOperation aborted",
                    agent.getHostname() ) );
            return;
        }

        boolean ok = false;
        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            ok = uninstall( agent );
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {
            ok = destroyNode( agent );
        }
        else
        {
            po.addLog( "Undefined setup type" );
        }

        if ( ok )
        {
            config.getNodes().remove( agent );
            po.addLog( "Updating db..." );

            manager.getPluginDao().saveInfo( NutchConfig.PRODUCT_KEY, config.getClusterName(), config );
            po.addLogDone( "Cluster info updated in DB\nDone" );
        }
        else
        {
            po.addLogFailed( "Failed to destroy node" );
        }
    }


    private boolean uninstall( Agent agent )
    {
        ProductOperation po = productOperation;
        po.addLog( "Uninstalling " + NutchConfig.PRODUCT_KEY );

        Command cmd = manager.getCommands().getUninstallCommand( Sets.newHashSet( agent ) );
        manager.getCommandRunner().runCommand( cmd );

        if ( cmd.hasSucceeded() )
        {
            po.addLog( NutchConfig.PRODUCT_KEY + " removed from " + agent.getHostname() );
            return true;
        }
        else
        {
            po.addLog( "Uninstallation failed: " + cmd.getAllErrors() );
            return false;
        }
    }


    private boolean destroyNode( Agent agent )
    {
        try
        {
            manager.getContainerManager().cloneDestroy( agent.getParentHostName(), agent.getHostname() );
            return true;
        }
        catch ( LxcDestroyException ex )
        {
            productOperation.addLog( "Failed to destroy node: " + ex.getMessage() );
            return false;
        }
    }
}
