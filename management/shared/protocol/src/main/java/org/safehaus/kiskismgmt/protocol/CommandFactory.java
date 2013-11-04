/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskismgmt.protocol;

/**
 *
 * @author dilshat
 */
import java.util.List;
import java.util.Map;

public class CommandFactory {
    
    public static ICommand createRequest(RequestType type, String uuid, String source, Long reqSeqNum,
            String workDir, String program, OutputRedirection stdOut, OutputRedirection stdErr, String stdOutPath, String stdErrPath, String runAs, List<String> args, Map<String, String> envVars) {
        Request req = new Request();
        req.setSource(source);
        req.setType(type);
        req.setUuid(uuid);
        req.setRequestSequenceNumber(reqSeqNum);
        req.setWorkingDirectory(workDir);
        req.setProgram(program);
        req.setStdOut(stdOut);
        req.setStdErr(stdErr);
        req.setStdOutPath(stdOutPath);
        req.setStdErrPath(stdErrPath);
        req.setRunAs(runAs);
        req.setArgs(args);
        req.setEnvironment(envVars);
        
        return new Command(req);
    }
    
    public static ICommand createResponse(ResponseType type, String uuid, String source, Integer exitCode, String stdOut, String stdErr, Long reqSeqNum, Long resSeqnum, Integer pid, String macAddress, String hostname, List<String> ips) {
        Response res = new Response();
        res.setSource(source);
        res.setType(type);
        res.setUuid(uuid);
        res.setExitCode(exitCode);
        res.setStdOut(stdOut);
        res.setStdErr(stdErr);
        res.setRequestSequenceNumber(reqSeqNum);
        res.setResponseSequenceNumber(resSeqnum);
        res.setPid(pid);
        res.setMacAddress(macAddress);
        res.setHostname(hostname);
        res.setIps(ips);
        
        return new Command(res);
    }
}
