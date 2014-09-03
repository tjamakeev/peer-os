package org.safehaus.subutai.plugin.spark.impl.handler;

import org.safehaus.subutai.core.commandrunner.api.AgentResult;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.Commands;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;

import java.util.UUID;

public class CheckNodeOperationHandler extends AbstractOperationHandler<SparkImpl> {

	private final String lxcHostname;

	public CheckNodeOperationHandler(SparkImpl manager, String clusterName, String lxcHostname) {
		super(manager, clusterName);
		this.lxcHostname = lxcHostname;
		productOperation = manager.getTracker().createProductOperation(SparkClusterConfig.PRODUCT_KEY,
				String.format("Checking state of %s in %s", lxcHostname, clusterName));
	}

	@Override
	public UUID getTrackerId() {
		return productOperation.getId();
	}

	@Override
	public void run() {
		SparkClusterConfig config = manager.getCluster(clusterName);
		if (config == null) {
			productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
			return;
		}

		Agent node = manager.getAgentManager().getAgentByHostname(lxcHostname);
		if (node == null) {
			productOperation.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
			return;
		}

		if (!config.getAllNodes().contains(node)) {
			productOperation.addLogFailed( String.format( "Node %s does not belong to this cluster", lxcHostname ) );
			return;
		}

		productOperation.addLog( "Checking node..." );

		Command checkNodeCommand = Commands.getStatusAllCommand(node);
		manager.getCommandRunner().runCommand(checkNodeCommand);

		AgentResult res = checkNodeCommand.getResults().get(node.getUuid());
		if (checkNodeCommand.hasSucceeded()) {
			productOperation.addLogDone( String.format( "%s", res.getStdOut() ) );
		} else {
			productOperation.addLogFailed( String.format( "Faied to check status, %s", checkNodeCommand.getAllErrors() ) );
		}
	}
}
