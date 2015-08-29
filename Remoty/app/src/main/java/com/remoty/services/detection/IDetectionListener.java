package com.remoty.services.detection;

import com.remoty.common.datatypes.ServerInfo;

import java.util.List;

/**
 * Created by Bogdan on 8/22/2015.
 */
public interface IDetectionListener {

    void update(List<ServerInfo> servers);
}
