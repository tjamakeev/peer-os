package org.safehaus.kiskis.mgmt.impl.hive.handler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hive.CommandType;
import org.safehaus.kiskis.mgmt.impl.hive.Commands;
import org.safehaus.kiskis.mgmt.impl.hive.HiveImpl;
import org.safehaus.kiskis.mgmt.impl.hive.Product;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class DestroyNodeHandler extends AbstractHandler {

    private final String hostname;
    private final ProductOperation po;

    public DestroyNodeHandler(HiveImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.hostname = hostname;
        this.po = manager.getTracker().createProductOperation(
                Config.PRODUCT_KEY, "Remove node from cluster: " + hostname);
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    public void run() {
        Config config = manager.getCluster(clusterName);
        if(config == null) {
            po.addLogFailed(String.format("Cluster '%s' does not exist", clusterName));
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            po.addLogFailed(String.format("Node '%s' is not connected", hostname));
            return;
        }

        if(config.getClients().size() == 1) {
            po.addLog("This is the last node in cluster. Destroy cluster instead");
            return;
        }

        String s = Commands.make(CommandType.PURGE, Product.HIVE);
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s),
                new HashSet<Agent>(Arrays.asList(agent)));
        manager.getCommandRunner().runCommand(cmd);

        AgentResult res = cmd.getResults().get(agent.getUuid());
        po.addLog(res.getStdOut());
        po.addLog(res.getStdErr());

        if(cmd.hasSucceeded()) {
            config.getClients().remove(agent);
            po.addLog("Done");

            po.addLog("Update cluster info...");
            if(manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config))
                po.addLogDone("Cluster info successfully updated");
            else
                po.addLogFailed("Failed to update cluster info");

        }
    }

}
