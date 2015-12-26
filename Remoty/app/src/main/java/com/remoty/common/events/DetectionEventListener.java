package com.remoty.common.events;

import com.remoty.common.datatypes.ServerInfo;

import java.util.List;


public abstract class DetectionEventListener implements IEventListener<DetectionEvent> {

	@Override
	public void notify(DetectionEvent event) {

		update(event.getServers());
	}

	public abstract void update(List<ServerInfo> servers);
}
