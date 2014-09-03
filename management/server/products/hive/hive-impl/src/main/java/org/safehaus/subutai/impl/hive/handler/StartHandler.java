package org.safehaus.subutai.impl.hive.handler;

import org.safehaus.subutai.core.commandrunner.api.AgentResult;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.core.commandrunner.api.RequestBuilder;
import org.safehaus.subutai.api.hive.Config;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.impl.hive.CommandType;
import org.safehaus.subutai.impl.hive.Commands;
import org.safehaus.subutai.impl.hive.HiveImpl;
import org.safehaus.subutai.impl.hive.Product;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Arrays;
import java.util.HashSet;

public class StartHandler extends AbstractHandler {

	private final String hostname;

	public StartHandler(HiveImpl manager, String clusterName, String hostname) {
		super(manager, clusterName);
		this.hostname = hostname;
		this.productOperation = manager.getTracker().createProductOperation(
				Config.PRODUCT_KEY, "Start node " + hostname);
	}

	@Override
	public void run() {
		ProductOperation po = productOperation;
		Config config = manager.getCluster(clusterName);
		if (config == null) {
			po.addLogFailed(String.format("Cluster '%s' does not exist",
					clusterName));
			return;
		}

		Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
		if (agent == null) {
			po.addLogFailed(String.format("Node '%s' is not connected", hostname));
			return;
		}

		boolean ok = true;

		// if server node, start Derby first
		if (agent.equals(config.getServer())) {
			String s = Commands.make(CommandType.START, Product.DERBY);
			Command cmd = manager.getCommandRunner().createCommand(
					new RequestBuilder(s).withTimeout(60),
					new HashSet<>(Arrays.asList(agent)));
			manager.getCommandRunner().runCommand(cmd);

			AgentResult res = cmd.getResults().get(agent.getUuid());
			po.addLog(res.getStdOut());
			po.addLog(res.getStdErr());

			ok = cmd.hasSucceeded();
		}
		if (ok) {

			String s = Commands.make(CommandType.START, Product.HIVE);
			Command cmd = manager.getCommandRunner().createCommand(
					new RequestBuilder(s).withTimeout(60),
					new HashSet<>(Arrays.asList(agent)));
			manager.getCommandRunner().runCommand(cmd);

			AgentResult res = cmd.getResults().get(agent.getUuid());
			po.addLog(res.getStdOut());
			po.addLog(res.getStdErr());

			ok = cmd.hasSucceeded();
		}

		if (ok) po.addLogDone("Done");
		else po.addLogFailed(null);
	}

}
