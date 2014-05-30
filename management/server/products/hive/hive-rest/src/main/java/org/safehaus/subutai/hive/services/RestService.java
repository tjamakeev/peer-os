package org.safehaus.subutai.hive.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("hive")
public interface RestService {

    @GET
    @Path("install/{clusterName}")
    public String installCluster(@PathParam("clusterName") String clusterName);

    @GET
    @Path("uninstall/{clusterName}") //Maps for the `hello/John` in the URL
    public String uninstallCluster(@PathParam("clusterName") String clusterName);


}