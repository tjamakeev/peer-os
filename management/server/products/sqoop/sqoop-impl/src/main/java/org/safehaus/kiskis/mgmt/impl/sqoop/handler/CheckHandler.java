package org.safehaus.kiskis.mgmt.impl.sqoop.handler;

import java.util.Arrays;
import java.util.HashSet;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.sqoop.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.sqoop.CommandFactory;
import org.safehaus.kiskis.mgmt.impl.sqoop.CommandType;
import org.safehaus.kiskis.mgmt.impl.sqoop.SqoopImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class CheckHandler extends AbstractHandler {

    public CheckHandler(SqoopImpl manager, String clusterName, ProductOperation po) {
        super(manager, clusterName, po);
    }

    public void run() {
        Config config = getClusterConfig();
        if(config == null) {
            po.addLogFailed("Cluster not found: " + clusterName);
            return;
        }
        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            po.addLogFailed("Node is not connected");
            return;
        }

        String s = CommandFactory.build(CommandType.LIST, null);
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s),
                new HashSet<Agent>(Arrays.asList(agent)));

        manager.getCommandRunner().runCommand(cmd);

        AgentResult res = cmd.getResults().get(agent.getUuid());
        if(cmd.hasSucceeded()) {
            if(res.getStdOut().contains(CommandFactory.PACKAGE_NAME))
                po.addLogDone("Sqoop installed on " + hostname);
            else
                po.addLogFailed("Sqoop not installed on " + hostname);
        } else {
            po.addLog(res.getStdOut());
            po.addLogFailed(res.getStdErr());
        }

    }

}
