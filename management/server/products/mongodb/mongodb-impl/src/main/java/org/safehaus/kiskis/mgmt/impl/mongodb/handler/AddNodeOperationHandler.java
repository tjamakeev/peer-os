package org.safehaus.kiskis.mgmt.impl.mongodb.handler;

import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.mongodb.Config;
import org.safehaus.kiskis.mgmt.api.mongodb.NodeType;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.mongodb.MongoImpl;
import org.safehaus.kiskis.mgmt.impl.mongodb.common.CommandType;
import org.safehaus.kiskis.mgmt.impl.mongodb.common.Commands;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dilshat on 5/6/14.
 */
public class AddNodeOperationHandler extends AbstractOperationHandler<MongoImpl> {
    private final ProductOperation po;
    private final NodeType nodeType;


    public AddNodeOperationHandler(MongoImpl manager, String clusterName, NodeType nodeType) {
        super(manager, clusterName);
        this.nodeType = nodeType;
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Adding %s to %s", nodeType, clusterName));
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        Config config = manager.getCluster(clusterName);
        if (config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
            return;
        }
        if (nodeType == NodeType.DATA_NODE && config.getDataNodes().size() == 7) {
            po.addLogFailed("Replica set cannot have more than 7 members.\nOperation aborted");
            return;
        }
        try {

            po.addLog("Creating lxc container");

            Map<Agent, Set<Agent>> lxcAgentsMap = manager.getLxcManager().createLxcs(1);

            Agent agent = lxcAgentsMap.entrySet().iterator().next().getValue().iterator().next();

            if (nodeType == NodeType.DATA_NODE) {
                config.getDataNodes().add(agent);
            } else if (nodeType == NodeType.CONFIG_NODE) {
                config.getConfigServers().add(agent);
            } else if (nodeType == NodeType.ROUTER_NODE) {
                config.getRouterServers().add(agent);
            }
            po.addLog("Lxc container created successfully\nUpdating db...");
            if (manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                po.addLog("Cluster info updated in DB\nInstalling Mongo");
                //start addition of node
                if (nodeType == NodeType.DATA_NODE) {
                    addDataNode(po, config, agent);
                } else if (nodeType == NodeType.ROUTER_NODE) {
                    addRouter(po, config, agent);
                }
            } else {
                po.addLogFailed("Error while updating cluster info in DB. Check logs. Use LXC Module to cleanup\nFailed");
            }
        } catch (LxcCreateException ex) {
            po.addLogFailed(ex.getMessage());
        }
    }


    private void addDataNode(ProductOperation po, final Config config, Agent agent) {
        List<Command> commands = Commands.getAddDataNodeCommands(config, agent);

        boolean additionOK = true;
        Command findPrimaryNodeCommand = null;

        for (Command command : commands) {
            po.addLog(String.format("Running command: %s", command.getDescription()));
            final AtomicBoolean commandOK = new AtomicBoolean();

            if (command.getData() == CommandType.FIND_PRIMARY_NODE) {
                findPrimaryNodeCommand = command;
            }

            if (command.getData() == CommandType.START_DATA_NODES) {
                manager.getCommandRunner().runCommand(command, new CommandCallback() {

                    @Override
                    public void onResponse(Response response, AgentResult agentResult, Command command) {
                        if (agentResult.getStdOut().contains(
                                "child process started successfully, parent exiting")) {
                            commandOK.set(true);
                            stop();
                        }
                    }

                });
            } else {
                manager.getCommandRunner().runCommand(command);
            }

            if (command.hasSucceeded() || commandOK.get()) {
                po.addLog(String.format("Command %s succeeded", command.getDescription()));
            } else {
                po.addLog(String.format("Command %s failed: %s", command.getDescription(), command.getAllErrors()));
                additionOK = false;
                break;
            }

        }

        //parse result of findPrimaryNodeCommand
        if (additionOK) {
            if (findPrimaryNodeCommand != null && !findPrimaryNodeCommand.getResults().isEmpty()) {
                Agent primaryNodeAgent = null;
                Pattern p = Pattern.compile("primary\" : \"(.*)\"");
                AgentResult result = findPrimaryNodeCommand.getResults().entrySet().iterator().next().getValue();
                Matcher m = p.matcher(result.getStdOut());
                if (m.find()) {
                    String primaryNodeHost = m.group(1);
                    if (!Strings.isNullOrEmpty(primaryNodeHost)) {
                        String hostname = primaryNodeHost.split(":")[0].replace("." + config.getDomainName(), "");
                        primaryNodeAgent = manager.getAgentManager().getAgentByHostname(hostname);
                    }
                }

                if (primaryNodeAgent != null) {
                    Command registerSecondaryNodeWithPrimaryCommand
                            = Commands.getRegisterSecondaryNodeWithPrimaryCommand(
                            agent, config.getDataNodePort(), config.getDomainName(), primaryNodeAgent);

                    manager.getCommandRunner().runCommand(registerSecondaryNodeWithPrimaryCommand);
                    if (registerSecondaryNodeWithPrimaryCommand.hasSucceeded()) {
                        po.addLogDone(String.format("Command %s succeeded\nNode addition succeeded",
                                registerSecondaryNodeWithPrimaryCommand.getDescription()));
                    } else {
                        po.addLogFailed(String.format("Command %s failed: %s\nNode addition failed",
                                registerSecondaryNodeWithPrimaryCommand.getDescription(),
                                registerSecondaryNodeWithPrimaryCommand.getAllErrors()));
                    }
                } else {
                    po.addLogFailed("Could not find primary node\nNode addition failed");
                }
            } else {
                po.addLogFailed("Could not find primary node\nNode addition failed");
            }
        } else {
            po.addLogFailed("Node addition failed");
        }

    }

    private void addRouter(ProductOperation po, final Config config, Agent agent) {
        List<Command> commands = Commands.getAddRouterCommands(config, agent);

        boolean additionOK = true;

        for (Command command : commands) {
            po.addLog(String.format("Running command: %s", command.getDescription()));
            final AtomicBoolean commandOK = new AtomicBoolean();

            if (command.getData() == CommandType.START_ROUTERS) {
                manager.getCommandRunner().runCommand(command, new CommandCallback() {

                    @Override
                    public void onResponse(Response response, AgentResult agentResult, Command command) {
                        if (agentResult.getStdOut().contains(
                                "child process started successfully, parent exiting")) {
                            commandOK.set(true);
                            stop();
                        }
                    }

                });
            } else {
                manager.getCommandRunner().runCommand(command);
            }

            if (command.hasSucceeded() || commandOK.get()) {
                po.addLog(String.format("Command %s succeeded", command.getDescription()));
            } else {
                po.addLog(String.format("Command %s failed: %s", command.getDescription(), command.getAllErrors()));
                additionOK = false;
                break;
            }

        }

        if (additionOK) {
            po.addLogDone("Node addition succeeded");
        } else {
            po.addLogFailed("Node addition failed");
        }
    }
}
