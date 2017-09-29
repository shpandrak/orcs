package com.emc.ocopea.site.app;

/**
 * Created by liebea on 10/18/16.
 * Drink responsibly
 */
public enum DeployedAppServiceState {
    pending,
    queued,
    deploying,
    deployed,
    error,
    stopping,
    errorstopping,
    stopped
}
