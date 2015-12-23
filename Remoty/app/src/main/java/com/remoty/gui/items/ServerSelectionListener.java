package com.remoty.gui.items;

import com.remoty.common.other.ServerInfo;

/**
 * Created by Bogdan on 23/12/2015.
 */
public interface ServerSelectionListener {

	void serverSelected(ServerInfo server);

	void serverDeselected();
}
