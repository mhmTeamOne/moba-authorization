package com.mhm.clients;

import com.mhm.models.TokenModel;
import io.quarkus.rest.client.reactive.ClientFormParam;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


@Path("")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "keycloak-user-api")
public interface KeycloackUserClient {

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ClientFormParam(name = "client_id", value = "${quarkus.oidc.client-id}")
    @ClientFormParam(name = "client_secret", value = "${quarkus.oidc.credentials.secret}")
    @ClientFormParam(name = "grant_type", value = "password")
    Uni<Response> getUserToken(@FormParam("username") String username, @FormParam("password") String password);

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ClientFormParam(name = "client_id", value = "${quarkus.oidc.client-id}")
    @ClientFormParam(name = "client_secret", value = "${quarkus.oidc.credentials.secret}")
    @ClientFormParam(name = "grant_type", value = "client_credentials")
    Uni<TokenModel> getAdminToken();
}


