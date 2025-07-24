package com.mhm.clients;

import com.mhm.dto.UserCreateDTO;
import com.mhm.models.KeycloackUserDTO;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "user-api")
public interface UserClient {
    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "client_id", value = "${quarkus.oidc.client-id}")
    @ClientHeaderParam(name = "client_secret", value = "yXv5eh0EiOID1SqLSdNsrSmZh3KTbjXP")
    @ClientHeaderParam(name = "grant_type", value = "client_credentials")
    Uni<Response> registerUser(@HeaderParam("Authorization") String apiKey, KeycloackUserDTO keycloackUserDTO);

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "client_id", value = "${quarkus.oidc.client-id}")
    @ClientHeaderParam(name = "client_secret", value = "yXv5eh0EiOID1SqLSdNsrSmZh3KTbjXP")
    @ClientHeaderParam(name = "grant_type", value = "client_credentials")
    Uni<Response> deleteUser(@PathParam("id") String id, @HeaderParam("Authorization") String apiKey);
}
