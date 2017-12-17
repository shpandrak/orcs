package com.emc.nazgul.shpanpaas.h2;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by liebea on 1/4/16.
 * Drink responsibly
 */
@Path("/")
public interface H2DSBInternalWebAPI {

    @Consumes(MediaType.APPLICATION_JSON)
    @Path("copy-repo")
    @PUT
    Response updateCopyRepo(AddCopyRepositoryDTO addCopyRepositoryDTO);
}
