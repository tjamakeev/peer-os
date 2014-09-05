package org.safehaus.subutai.impl.hadoop.operation.configuration;

import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.api.hadoop.HadoopClusterConfig;
import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.impl.hadoop.Commands;
import org.safehaus.subutai.impl.hadoop.HadoopImpl;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.UUID;

/**
 * Created by daralbaev on 15.04.14.
 */
public class DataNode {
	private HadoopImpl parent;
	private HadoopClusterConfig hadoopClusterConfig;

	public DataNode(HadoopImpl parent, HadoopClusterConfig hadoopClusterConfig ) {
		this.parent = parent;
		this.hadoopClusterConfig = hadoopClusterConfig;
	}

	public UUID status(final Agent agent) {

		final ProductOperation po
				= parent.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
				String.format("Getting status of clusters %s DataNode", agent.getHostname()));

		parent.getExecutor().execute(new Runnable() {

			public void run() {

				final Agent node = parent.getAgentManager().getAgentByHostname(agent.getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", agent.getHostname()));
					return;
				}

				Command getNameNodeCommand = Commands.getNameNodeCommand(agent, "status");
				HadoopImpl.getCommandRunner().runCommand(getNameNodeCommand);

				NodeState nodeState = NodeState.UNKNOWN;
				if (getNameNodeCommand.hasCompleted()) {
					AgentResult result = getNameNodeCommand.getResults().get(agent.getUuid());
					if (result.getStdOut() != null && result.getStdOut().contains("DataNode")) {
						String[] array = result.getStdOut().split("\n");

						for (String status : array) {
							if (status.contains("DataNode")) {
								String temp = status.
										replaceAll("DataNode is ", "");
								if (temp.toLowerCase().contains("not")) {
									nodeState = NodeState.STOPPED;
								} else {
									nodeState = NodeState.RUNNING;
								}
							}
						}
					}
				}

				if (NodeState.UNKNOWN.equals(nodeState)) {
					po.addLogFailed(String.format("Failed to check status of %s",
							agent.getHostname()
					));
				} else {
					po.addLogDone(String.format("DataNode of %s is %s",
							agent.getHostname(),
							nodeState
					));
				}
			}
		});

		return po.getId();

	}

	public UUID block(final Agent agent) {

		final ProductOperation po
				= parent.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
				String.format("Blocking DataNode of %s cluster", agent.getHostname()));

		parent.getExecutor().execute(new Runnable() {

			public void run() {

				final Agent node = parent.getAgentManager().getAgentByHostname(agent.getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", agent.getHostname()));
					return;
				}

				Command command = Commands.getRemoveDataNodeCommand( hadoopClusterConfig, agent);
				HadoopImpl.getCommandRunner().runCommand(command);
				logCommand(command, po);

				command = Commands.getIncludeDataNodeCommand( hadoopClusterConfig, agent);
				HadoopImpl.getCommandRunner().runCommand(command);
				logCommand(command, po);

				command = Commands.getRefreshNameNodeCommand( hadoopClusterConfig );
				HadoopImpl.getCommandRunner().runCommand(command);
				logCommand(command, po);

				hadoopClusterConfig.getBlockedAgents().add(agent);
				if (parent.getDbManager().saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(),
                        hadoopClusterConfig )) {
					po.addLog("Cluster info saved to DB");
				} else {
					po.addLogFailed("Could not save cluster info to DB! Please see logs\n" +
							"Blocking node aborted");
				}


			}
		});

		return po.getId();

	}

	private void logCommand(Command command, ProductOperation po) {
		if (command.hasSucceeded()) {
			po.addLogDone(String.format("Task's operation %s finished", command.getDescription()));
		} else if (command.hasCompleted()) {
			po.addLogFailed(String.format("Task's operation %s failed", command.getDescription()));
		} else {
			po.addLogFailed(String.format("Task's operation %s timeout", command.getDescription()));
		}
	}

	public UUID unblock(final Agent agent) {

		final ProductOperation po
				= parent.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
				String.format("Unblocking DataNode of %s cluster", agent.getHostname()));

		parent.getExecutor().execute(new Runnable() {

			public void run() {

				final Agent node = parent.getAgentManager().getAgentByHostname(agent.getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", agent.getHostname()));
					return;
				}

				Command command = Commands.getSetDataNodeCommand( hadoopClusterConfig, agent);
				HadoopImpl.getCommandRunner().runCommand(command);
				logCommand(command, po);

				command = Commands.getExcludeDataNodeCommand( hadoopClusterConfig, agent);
				HadoopImpl.getCommandRunner().runCommand(command);
				logCommand(command, po);

				command = Commands.getStartNameNodeCommand(agent);
				HadoopImpl.getCommandRunner().runCommand(command);
				logCommand(command, po);

				hadoopClusterConfig.getBlockedAgents().remove(agent);
				if (parent.getDbManager().saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(),
                        hadoopClusterConfig )) {
					po.addLog("Cluster info saved to DB");
				} else {
					po.addLogFailed("Could not save cluster info to DB! Please see logs\n" +
							"Blocking node aborted");
				}
			}
		});

		return po.getId();

	}
}
