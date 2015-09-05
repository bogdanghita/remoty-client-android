package com.remoty.abc.events;

import com.remoty.common.ServerInfo;

import java.util.List;

/**
 * Created by Bogdan on 8/22/2015.
 */
public abstract class DetectionListener implements IServiceEventListener<DetectionEvent> {

	@Override
	public void notify(DetectionEvent event) {

		update(event.getServers());
	}

    public abstract void update(List<ServerInfo> servers);
}
