package org.safehaus.subutai.rest.template.manager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.aptrepositorymanager.AptRepoException;
import org.safehaus.subutai.api.aptrepositorymanager.AptRepositoryManager;
import org.safehaus.subutai.api.template.manager.TemplateManager;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.shared.protocol.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestServiceImpl implements RestService {

    private static final Logger logger = LoggerFactory.getLogger(RestServiceImpl.class);
    private String managementHostName = "management";
    TemplateManager templateManager;
    TemplateRegistryManager templateRegistry;
    AptRepositoryManager aptRepoManager;
    AgentManager agentManager;

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    public void setTemplateRegistry(TemplateRegistryManager templateRegistry) {
        this.templateRegistry = templateRegistry;
    }

    public void setAptRepoManager(AptRepositoryManager aptRepoManager) {
        this.aptRepoManager = aptRepoManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    @Override
    public String getManagementHostName() {
        return managementHostName;
    }

    @Override
    public void setManagementHostName(String managementHostName) {
        this.managementHostName = managementHostName;
    }

    @Override
    public String importTemplate(byte[] input) {
        Path path;
        try {
            path = Files.createTempFile("subutai-template-", ".deb");
            try(OutputStream os = new FileOutputStream(path.toFile())) {
                os.write(input);
            }
            logger.info("Payload saved to " + path.toString());
        } catch(IOException ex) {
            String m = "Failed to write payload data to file";
            logger.error(m, ex);
            return m;
        }

        Agent mgmt = agentManager.getAgentByHostname(managementHostName);
        try {
            //aptRepoManager.addPackageByPath(mgmt, path.toString(), false);

            List<String> conf = aptRepoManager.readFileContents(mgmt,
                    path.toString(), Arrays.asList("config"));
            List<String> pack = aptRepoManager.readFileContents(mgmt,
                    path.toString(), Arrays.asList("packages"));

            templateRegistry.registerTemplate(mergeLines(conf), mergeLines(pack));

        } catch(AptRepoException ex) {
            String m = "Failed to process deb package";
            logger.error(m, ex);
            return m;
        } catch(Exception ex) {
            String m = "Import of package failed";
            logger.error(m, ex);
            return m;
        } finally {
            // clean up
            path.toFile().delete();
        }
        return "Template package successfully imported.";
    }

    @Override
    public Response exportTemplate(String templateName) {
        // TODO: we need to be able to export in specified physical sever
        // currently this method calls export script on management server
        String path = templateManager.exportTemplate(managementHostName, templateName);
        if(path == null)
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

        String cd = "attachment; filename=\"" + Paths.get(path).getFileName() + "\"";
        return Response.ok(new File(path)).
                header("Content-Disposition", cd).
                type(MediaType.APPLICATION_OCTET_STREAM_TYPE).build();
    }

    private String mergeLines(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for(String s : lines) sb.append(s).append(System.lineSeparator());
        return sb.toString();
    }

}
