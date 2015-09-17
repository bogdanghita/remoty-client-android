package com.remoty.common.events;

import com.remoty.common.other.ServerInfo;

import java.util.List;

/**
 * Created by Bogdan on 8/22/2015.
 */
public abstract class DetectionEventListener implements IEventListener<DetectionEvent> {

	@Override
	public void notify(DetectionEvent event) {

		update(event.getServers());
	}

    public abstract void update(List<ServerInfo> servers);
}
