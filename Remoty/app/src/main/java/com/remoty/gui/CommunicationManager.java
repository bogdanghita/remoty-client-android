package com.remoty.gui;

/**
 * Created by Bogdan on 8/22/2015.
 */
public class CommunicationManager {

    /*
    There should be some class that:
     1. provides info about the connection
     2. provides method that sends an object continuously at a given interval
        - it should be given a socket, desired interval and max interval
        - it should analyze the process and adjust the interval
        - if the max interval is exceeded it should notify some listeners that this happened
        - it should also notify the listeners if the connection is lost
    3. provides method for sending single messages on demand
        - it would be nice if it could also analyze the process and identify if something is not ok
    4. methods to add and remove listeners
    5. method to set object that is to be sent by the continuous method
        - caution with the synchronization here
        - on set object: lock then unlock
        - on send: lock, clone object and then unlock
        - NOTE: this is just a suggestion, maybe you can find a better way

    This class (or an instance of it) should be shared between all the fragments. It also has to
    persist the info about the connection even if the app is closed and then restored (from a
    previous session). Don't know if it should persist other info or objects; we shall see...
     */
}
