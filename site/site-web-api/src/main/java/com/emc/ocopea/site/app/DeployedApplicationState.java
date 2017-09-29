package com.emc.ocopea.site.app;

/**
 * Created by liebea on 10/18/16.
 * Drink responsibly
 */
public enum DeployedApplicationState {
    pending,
    deploying,
    running,
    error,
    stopping,
    stopped,
    errorstopping
}
