package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.kiskis.mgmt.api.hbase.Config;
import org.safehaus.kiskis.mgmt.api.hbase.HBase;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.ProductOperationState;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.ProductOperationView;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;

import java.util.UUID;


/**
 * Displays the last log entries
 */
@Command(scope = "hbase", name = "uninstall-cluster", description = "Command to uninstall HBase cluster")
public class UninstallHBaseClusterCommand extends OsgiCommandSupport {

    private HBase hbaseManager;
    private Tracker tracker;

    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public HBase getHbaseManager() {
        return hbaseManager;
    }

    public void setHbaseManager(HBase hbaseManager) {
        this.hbaseManager = hbaseManager;
    }

    @Argument(index = 0, name = "clusterName", required = true, multiValued = false, description = "Delete cluster")
    String clusterName;

    protected Object doExecute() {

        UUID uuid = hbaseManager.uninstallCluster(clusterName);

        int logSize = 0;
        while (!Thread.interrupted()) {
            ProductOperationView po = tracker.getProductOperation(Config.PRODUCT_KEY, uuid);
            if (po != null) {
                if (logSize != po.getLog().length()) {
                    System.out.print(po.getLog().substring(logSize, po.getLog().length()));
                    System.out.flush();
                    logSize = po.getLog().length();
                }
                if (po.getState() != ProductOperationState.RUNNING) {
                    break;
                }
            } else {
                System.out.println("Product operation not found. Check logs");
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                break;
            }
        }
        return null;

    }
}
