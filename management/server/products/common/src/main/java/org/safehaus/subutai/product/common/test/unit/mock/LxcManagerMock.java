package org.safehaus.subutai.product.common.test.unit.mock;


import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.core.container.api.lxcmanager.*;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.*;


public class LxcManagerMock implements LxcManager {

	private Map<Agent, Set<Agent>> mockLxcMap = new HashMap<Agent, Set<Agent>>();


	public LxcManagerMock setMockLxcMap(Map<Agent, Set<Agent>> mockLxcMap) {
		this.mockLxcMap = mockLxcMap;
		return this;
	}

	@Override
	public Map<Agent, Integer> getPhysicalServersWithLxcSlots() {
		return null;
	}

	@Override
	public Map<Agent, ServerMetric> getPhysicalServerMetrics() {
		return null;
	}

	@Override
	public Map<String, EnumMap<LxcState, List<String>>> getLxcOnPhysicalServers() {
		return null;
	}

	@Override
	public boolean cloneLxcOnHost(Agent physicalAgent, String lxcHostname) {
		return false;
	}

	@Override
	public LxcState checkLxcOnHost(final Agent physicalAgent, final String lxcHostname) {
		return LxcState.UNKNOWN;
	}

	@Override
	public boolean startLxcOnHost(Agent physicalAgent, String lxcHostname) {
		return false;
	}

	@Override
	public boolean stopLxcOnHost(Agent physicalAgent, String lxcHostname) {
		return false;
	}

	@Override
	public boolean destroyLxcOnHost(Agent physicalAgent, String lxcHostname) {
		return false;
	}

	@Override
	public boolean cloneNStartLxcOnHost(Agent physicalAgent, String lxcHostname) {
		return false;
	}

	@Override
	public Map<Agent, Set<Agent>> createLxcs(int count) throws LxcCreateException {
		return mockLxcMap;
	}

	@Override
	public void destroyLxcsByHostname(Map<Agent, Set<String>> agentFamilies) throws LxcDestroyException {

	}


	@Override
	public void destroyLxcs(Map<Agent, Set<Agent>> agentFamilies) throws LxcDestroyException {

	}


	@Override
	public void destroyLxcs(Set<Agent> lxcAgents) throws LxcDestroyException {

	}


	@Override
	public void destroyLxcsByHostname(Set<String> lxcAgentHostnames) throws LxcDestroyException {

	}


	@Override
	public Map<String, Map<Agent, Set<Agent>>> createLxcsByStrategy(LxcPlacementStrategy strategy)
			throws LxcCreateException {
		return null;
	}


	@Override
	public AgentManager getAgentManager() {
		return null;
	}
}
