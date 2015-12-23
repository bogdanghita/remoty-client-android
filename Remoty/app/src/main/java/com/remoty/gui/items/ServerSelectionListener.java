package com.remoty.gui.items;

import com.remoty.common.other.ServerInfo;


public interface ServerSelectionListener {

	void serverSelected(ServerInfo server);

	void serverDeselected();
}
