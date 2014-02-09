/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.web.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author dilshat
 */
public class WebServer {

    private Server server;

    public void init() {
        if (server == null) {
            try {
                server = new Server();
                SelectChannelConnector connector = new SelectChannelConnector();
                connector.setPort(Common.WEB_SERVER_PORT);
                server.addConnector(connector);

                ResourceHandler resource_handler = new ResourceHandler();
                resource_handler.setDirectoriesListed(true);
                resource_handler.setResourceBase(System.getProperty("karaf.base") + "/" + Common.WEB_SERVER_RES_FOLDER);

                HandlerList handlers = new HandlerList();
                handlers.setHandlers(new Handler[]{resource_handler});
                server.setHandler(handlers);

                server.start();
            } catch (Exception ex) {
                Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void destroy() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception ex) {
                Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
