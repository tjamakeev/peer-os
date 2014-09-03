package org.safehaus.subutai.product.common.test.unit.mock;


import org.safehaus.subutai.core.agentmanager.api.AgentListener;
import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Set;
import java.util.UUID;


public class AgentManagerMock implements AgentManager {

	@Override
	public Set<Agent> getAgents() {
		return null;
	}


	@Override
	public Set<Agent> getPhysicalAgents() {
		return null;
	}


	@Override
	public Set<Agent> getLxcAgents() {
		return null;
	}


	@Override
	public Agent getAgentByHostname(String hostname) {
		return null;
	}


	@Override
	public Agent getAgentByUUID(UUID uuid) {
		return null;
	}


	@Override
	public Set<Agent> getLxcAgentsByParentHostname(String parentHostname) {
		return null;
	}


	@Override
	public void addListener(AgentListener listener) {

	}


	@Override
	public void removeListener(AgentListener listener) {

	}
}
