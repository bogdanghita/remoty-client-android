package com.remoty.common.events;

import com.remoty.common.servicemanager.EventManager;
import com.remoty.common.ServerInfo;

import java.util.List;

/**
 * Created by Bogdan on 9/5/2015.
 */
public class DetectionEvent extends BaseEvent {

	private List<ServerInfo> servers;

	public DetectionEvent(List<ServerInfo> servers) {
		super(EventManager.EventType.DETECTION);

		this.servers = servers;
	}

	public List<ServerInfo> getServers() {
		return servers;
	}

}
