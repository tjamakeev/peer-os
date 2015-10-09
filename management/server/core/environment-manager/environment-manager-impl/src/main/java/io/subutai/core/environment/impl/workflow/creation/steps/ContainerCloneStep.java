package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Sets;

import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.steps.helpers.CreatePeerNodeGroupsTask;
import io.subutai.core.environment.impl.workflow.creation.steps.helpers.NodeGroupBuildResult;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.registry.api.TemplateRegistry;


/**
 * Container creation step
 */
public class ContainerCloneStep
{

    private final TemplateRegistry templateRegistry;
    private final String defaultDomain;
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final LocalPeer localPeer;
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();


    public ContainerCloneStep( final TemplateRegistry templateRegistry, final String defaultDomain,
                               final Topology topology, final EnvironmentImpl environment, final LocalPeer localPeer )
    {
        this.templateRegistry = templateRegistry;
        this.defaultDomain = defaultDomain;
        this.topology = topology;
        this.environment = environment;
        this.localPeer = localPeer;
    }


    public void execute() throws EnvironmentCreationException
    {

        Map<Peer, Set<NodeGroup>> placement = topology.getNodeGroupPlacement();

        SubnetUtils cidr = new SubnetUtils( environment.getSubnetCidr() );

        //obtain available ip address count
        int totalAvailableIpCount = cidr.getInfo().getAddressCount() - 1;//one ip is for gateway

        //subtract already used ip range
        totalAvailableIpCount -= environment.getLastUsedIpIndex();

        //obtain used ip address count
        int requestedContainerCount = 0;
        for ( Set<NodeGroup> nodeGroups : placement.values() )
        {
            for ( NodeGroup nodeGroup : nodeGroups )
            {
                requestedContainerCount += nodeGroup.getNumberOfContainers();
            }
        }

        //check if available ip addresses are enough
        if ( requestedContainerCount > totalAvailableIpCount )
        {
            throw new EnvironmentCreationException(
                    String.format( "Requested %d containers but only %d ip addresses available",
                            requestedContainerCount, totalAvailableIpCount ) );
        }

        ExecutorService taskExecutor = getExecutor( placement.size() );

        CompletionService<Set<NodeGroupBuildResult>> taskCompletionService = getCompletionService( taskExecutor );

        int currentLastUsedIpIndex = environment.getLastUsedIpIndex();


        //submit parallel environment part creation tasks across peers
        for ( Map.Entry<Peer, Set<NodeGroup>> peerPlacement : placement.entrySet() )
        {
            Peer peer = peerPlacement.getKey();

            taskCompletionService.submit(
                    new CreatePeerNodeGroupsTask( peer, peerPlacement.getValue(), localPeer, environment,
                            currentLastUsedIpIndex + 1, templateRegistry, defaultDomain ) );

            for ( NodeGroup nodeGroup : peerPlacement.getValue() )
            {
                currentLastUsedIpIndex += nodeGroup.getNumberOfContainers();
            }


            environment.setLastUsedIpIndex( currentLastUsedIpIndex );
        }

        //collect results
        Set<String> errors = Sets.newHashSet();

        for ( int i = 0; i < placement.size(); i++ )
        {
            try
            {
                Future<Set<NodeGroupBuildResult>> futures = taskCompletionService.take();
                Set<NodeGroupBuildResult> results = futures.get();
                for ( NodeGroupBuildResult result : results )
                {
                    if ( !CollectionUtil.isCollectionEmpty( result.getContainers() ) )
                    {
                        environment.addContainers( result.getContainers() );
                    }

                    if ( result.getException() != null )
                    {
                        errors.add( exceptionUtil.getRootCauseMessage( result.getException() ) );
                    }
                }
            }
            catch ( ExecutionException | InterruptedException e )
            {
                errors.add( exceptionUtil.getRootCauseMessage( e ) );
            }
        }

        taskExecutor.shutdown();

        if ( !errors.isEmpty() )
        {

            throw new EnvironmentCreationException(
                    String.format( "There were errors during container creation:  %s", errors ) );
        }
    }


    protected ExecutorService getExecutor( int numOfThreads )
    {
        return Executors.newFixedThreadPool( numOfThreads );
    }


    protected CompletionService<Set<NodeGroupBuildResult>> getCompletionService( Executor executor )
    {
        return new ExecutorCompletionService<>( executor );
    }
}