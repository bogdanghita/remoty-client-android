package com.remoty.common;

import android.os.Bundle;

/**
 * Created by Bogdan on 8/22/2015.
 */
public class ServerInfo {

    // TODO: ...

    public ServerInfo(String ip, int port, String name){

    }
    
    /**
     * @param info   - the info to be saved.
     * @param bundle - the Bundle where the info will be saved.
     */
    public static void saveToBundle(ServerInfo info, Bundle bundle) {

    }

    /**
     * @param bundle - the Bundle to retrieve the info from.
     * @return - the info retrieved from the Bundle and null if the Bundle does not contain the ServerInfo data.
     */
    public static ServerInfo retrieveFromBundle(Bundle bundle) {
        return null;
    }
}
