package com.emc.ocopea.site.artifact;

import java.util.Collection;

/**
 * Created by liebea on 1/15/17.
 * Drink responsibly
 */
public interface ArtifactRegistryApi {
    Collection<String> listVersions(String artifactId);
}
