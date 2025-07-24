package com.mhm.clients;

import com.mhm.models.KeycloackUserDTO;
import com.mhm.models.TokenModel;
import io.quarkus.rest.client.reactive.ClientFormParam;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;
import java.util.Map;

@RegisterRestClient(configKey = "keycloak-admin-api")
public interface KeycloakAdminClient {

    // =================================
    // TOKEN OPERATIONS
    // =================================
    
    @POST
    @Path("/realms/moba/protocol/openid-connect/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientFormParam(name = "client_id", value = "${quarkus.oidc.client-id}")
    @ClientFormParam(name = "client_secret", value = "${quarkus.oidc.credentials.secret}")
    @ClientFormParam(name = "grant_type", value = "password")
    Uni<Response> getUserToken(@FormParam("username") String username, @FormParam("password") String password);

    @POST
    @Path("/realms/moba/protocol/openid-connect/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientFormParam(name = "client_id", value = "${quarkus.oidc.client-id}")
    @ClientFormParam(name = "client_secret", value = "${quarkus.oidc.credentials.secret}")
    @ClientFormParam(name = "grant_type", value = "client_credentials")
    Uni<TokenModel> getAdminToken();

    @POST
    @Path("/realms/moba/protocol/openid-connect/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientFormParam(name = "client_id", value = "${quarkus.oidc.client-id}")
    @ClientFormParam(name = "client_secret", value = "${quarkus.oidc.credentials.secret}")
    @ClientFormParam(name = "grant_type", value = "refresh_token")
    Uni<TokenModel> refreshToken(@FormParam("refresh_token") String refreshToken);

    @POST
    @Path("/realms/moba/protocol/openid-connect/logout")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ClientFormParam(name = "client_id", value = "${quarkus.oidc.client-id}")
    @ClientFormParam(name = "client_secret", value = "${quarkus.oidc.credentials.secret}")
    Uni<Response> logoutUser(@FormParam("refresh_token") String refreshToken);

    @POST
    @Path("/realms/moba/protocol/openid-connect/token/introspect")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientFormParam(name = "client_id", value = "${quarkus.oidc.client-id}")
    @ClientFormParam(name = "client_secret", value = "${quarkus.oidc.credentials.secret}")
    Uni<Map<String, Object>> introspectToken(@FormParam("token") String token);

    // =================================
    // USER MANAGEMENT
    // =================================

    @POST
    @Path("/admin/realms/moba/users")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> createUser(@HeaderParam("Authorization") String adminToken, KeycloackUserDTO user);

    @GET
    @Path("/admin/realms/moba/users/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<KeycloackUserDTO> getUserById(@HeaderParam("Authorization") String adminToken, @PathParam("id") String userId);

    @GET
    @Path("/admin/realms/moba/users")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<KeycloackUserDTO>> getUsers(@HeaderParam("Authorization") String adminToken,
                                         @QueryParam("username") String username,
                                         @QueryParam("email") String email,
                                         @QueryParam("firstName") String firstName,
                                         @QueryParam("lastName") String lastName,
                                         @QueryParam("first") Integer first,
                                         @QueryParam("max") Integer max);

    @PUT
    @Path("/admin/realms/moba/users/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> updateUser(@HeaderParam("Authorization") String adminToken, @PathParam("id") String userId, KeycloackUserDTO user);

    @DELETE
    @Path("/admin/realms/moba/users/{id}")
    Uni<Response> deleteUser(@HeaderParam("Authorization") String adminToken, @PathParam("id") String userId);

    @GET
    @Path("/admin/realms/moba/users/count")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Integer> getUsersCount(@HeaderParam("Authorization") String adminToken);

    @PUT
    @Path("/admin/realms/moba/users/{id}/enabled")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Response> enableUser(@HeaderParam("Authorization") String adminToken, @PathParam("id") String userId, boolean enabled);

    @PUT
    @Path("/admin/realms/moba/users/{id}/reset-password")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Response> resetPassword(@HeaderParam("Authorization") String adminToken, @PathParam("id") String userId, Map<String, Object> passwordData);

    @POST
    @Path("/admin/realms/moba/users/{id}/execute-actions-email")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Response> sendExecuteActionsEmail(@HeaderParam("Authorization") String adminToken, @PathParam("id") String userId, List<String> actions);

    @POST
    @Path("/admin/realms/moba/users/{id}/send-verify-email")
    Uni<Response> sendVerifyEmail(@HeaderParam("Authorization") String adminToken, @PathParam("id") String userId);

    // =================================
    // USER SESSIONS
    // =================================

    @GET
    @Path("/admin/realms/moba/users/{id}/sessions")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<Map<String, Object>>> getUserSessions(@HeaderParam("Authorization") String adminToken, @PathParam("id") String userId);

    @DELETE
    @Path("/admin/realms/moba/users/{id}/sessions")
    Uni<Response> logoutAllUserSessions(@HeaderParam("Authorization") String adminToken, @PathParam("id") String userId);

    // =================================
    // GROUPS MANAGEMENT
    // =================================

    @GET
    @Path("/admin/realms/moba/groups")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<Map<String, Object>>> getGroups(@HeaderParam("Authorization") String adminToken);

    @POST
    @Path("/admin/realms/moba/groups")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Response> createGroup(@HeaderParam("Authorization") String adminToken, Map<String, Object> group);

    @GET
    @Path("/admin/realms/moba/groups/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Map<String, Object>> getGroupById(@HeaderParam("Authorization") String adminToken, @PathParam("id") String groupId);

    @PUT
    @Path("/admin/realms/moba/groups/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Response> updateGroup(@HeaderParam("Authorization") String adminToken, @PathParam("id") String groupId, Map<String, Object> group);

    @DELETE
    @Path("/admin/realms/moba/groups/{id}")
    Uni<Response> deleteGroup(@HeaderParam("Authorization") String adminToken, @PathParam("id") String groupId);

    @GET
    @Path("/admin/realms/moba/users/{id}/groups")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<Map<String, Object>>> getUserGroups(@HeaderParam("Authorization") String adminToken, @PathParam("id") String userId);

    @PUT
    @Path("/admin/realms/moba/users/{userId}/groups/{groupId}")
    Uni<Response> addUserToGroup(@HeaderParam("Authorization") String adminToken, @PathParam("userId") String userId, @PathParam("groupId") String groupId);

    @DELETE
    @Path("/admin/realms/moba/users/{userId}/groups/{groupId}")
    Uni<Response> removeUserFromGroup(@HeaderParam("Authorization") String adminToken, @PathParam("userId") String userId, @PathParam("groupId") String groupId);

    // =================================
    // ROLES MANAGEMENT
    // =================================

    @GET
    @Path("/admin/realms/moba/roles")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<Map<String, Object>>> getRoles(@HeaderParam("Authorization") String adminToken);

    @POST
    @Path("/admin/realms/moba/roles")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Response> createRole(@HeaderParam("Authorization") String adminToken, Map<String, Object> role);

    @GET
    @Path("/admin/realms/moba/roles/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Map<String, Object>> getRoleByName(@HeaderParam("Authorization") String adminToken, @PathParam("name") String roleName);

    @PUT
    @Path("/admin/realms/moba/roles/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Response> updateRole(@HeaderParam("Authorization") String adminToken, @PathParam("name") String roleName, Map<String, Object> role);

    @DELETE
    @Path("/admin/realms/moba/roles/{name}")
    Uni<Response> deleteRole(@HeaderParam("Authorization") String adminToken, @PathParam("name") String roleName);

    @GET
    @Path("/admin/realms/moba/users/{id}/role-mappings/realm")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<Map<String, Object>>> getUserRoles(@HeaderParam("Authorization") String adminToken, @PathParam("id") String userId);

    @POST
    @Path("/admin/realms/moba/users/{id}/role-mappings/realm")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Response> addRolesToUser(@HeaderParam("Authorization") String adminToken, @PathParam("id") String userId, List<Map<String, Object>> roles);

    @DELETE
    @Path("/admin/realms/moba/users/{id}/role-mappings/realm")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Response> removeRolesFromUser(@HeaderParam("Authorization") String adminToken, @PathParam("id") String userId, List<Map<String, Object>> roles);

    // =================================
    // REALM MANAGEMENT
    // =================================

    @GET
    @Path("/admin/realms/moba")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Map<String, Object>> getRealmInfo(@HeaderParam("Authorization") String adminToken);

    @PUT
    @Path("/admin/realms/moba")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Response> updateRealm(@HeaderParam("Authorization") String adminToken, Map<String, Object> realmData);

    @GET
    @Path("/admin/realms/moba/events")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<Map<String, Object>>> getEvents(@HeaderParam("Authorization") String adminToken,
                                             @QueryParam("type") String type,
                                             @QueryParam("client") String client,
                                             @QueryParam("user") String user,
                                             @QueryParam("first") Integer first,
                                             @QueryParam("max") Integer max);

    @DELETE
    @Path("/admin/realms/moba/events")
    Uni<Response> clearEvents(@HeaderParam("Authorization") String adminToken);

    // =================================
    // CLIENTS MANAGEMENT
    // =================================

    @GET
    @Path("/admin/realms/moba/clients")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<Map<String, Object>>> getClients(@HeaderParam("Authorization") String adminToken);

    @POST
    @Path("/admin/realms/moba/clients")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Response> createClient(@HeaderParam("Authorization") String adminToken, Map<String, Object> client);

    @GET
    @Path("/admin/realms/moba/clients/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Map<String, Object>> getClientById(@HeaderParam("Authorization") String adminToken, @PathParam("id") String clientId);

    @PUT
    @Path("/admin/realms/moba/clients/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Response> updateClient(@HeaderParam("Authorization") String adminToken, @PathParam("id") String clientId, Map<String, Object> client);

    @DELETE
    @Path("/admin/realms/moba/clients/{id}")
    Uni<Response> deleteClient(@HeaderParam("Authorization") String adminToken, @PathParam("id") String clientId);

    @GET
    @Path("/admin/realms/moba/clients/{id}/user-sessions")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<Map<String, Object>>> getClientUserSessions(@HeaderParam("Authorization") String adminToken, @PathParam("id") String clientId);

} 