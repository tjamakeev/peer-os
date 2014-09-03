package org.safehaus.subutai.plugin.cassandra.impl.handler;


import java.util.UUID;

import com.google.common.collect.Sets;
import org.safehaus.subutai.plugin.cassandra.api.CassandraConfig;
import org.safehaus.subutai.core.commandrunner.api.AgentResult;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.safehaus.subutai.plugin.cassandra.impl.Commands;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Agent;


/**
 * Created by bahadyr on 8/25/14.
 */
public class StartCassandraServiceOperationHandler extends AbstractOperationHandler<CassandraImpl> {

    private ProductOperation po;
    //    private CassandraConfig config;
    private String agentUUID;

    private String clusterName;


    public StartCassandraServiceOperationHandler( final CassandraImpl manager, final String clusterName, final String agentUUID) {
        super( manager, clusterName );
        this.agentUUID = agentUUID;
        this.clusterName = clusterName;
        po = manager.getTracker().createProductOperation( CassandraConfig.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
    }


    @Override
    public void run() {
        final ProductOperation po = manager.getTracker().createProductOperation( CassandraConfig.PRODUCT_KEY,
                String.format( "Starting Cassandra service on %s", agentUUID ) );
        manager.getExecutor().execute( new Runnable() {
            Agent agent = manager.getAgentManager().getAgentByUUID( UUID.fromString( agentUUID ) );


            public void run() {
                Command startServiceCommand = Commands.getStartCommand(Sets.newHashSet(agent) );
                manager.getCommandRunner().runCommand( startServiceCommand );
                if ( startServiceCommand.hasSucceeded() ) {
                    AgentResult ar = startServiceCommand.getResults().get( agent.getUuid() );
                    if ( ar.getStdOut().contains( "starting Cassandra ..." ) || ar.getStdOut().contains(
                            "is already running..." ) ) {
                        po.addLog( ar.getStdOut() );
                        po.addLogDone( "Start succeeded" );
                    }
                }
                else {
                    po.addLogFailed( String.format( "Start failed, %s", startServiceCommand.getAllErrors() ) );
                }
            }
        } );
    }
}