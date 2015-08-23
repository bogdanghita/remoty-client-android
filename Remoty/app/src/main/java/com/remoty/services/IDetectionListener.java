package com.remoty.services;

import com.remoty.common.ServerInfo;

import java.util.List;

/**
 * Created by Bogdan on 8/22/2015.
 */
public interface IDetectionListener {

    void update(List<ServerInfo> servers);
}
