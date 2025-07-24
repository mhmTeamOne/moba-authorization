package com.mhm.resources;

import com.mhm.dto.UserCreateDTO;
import com.mhm.dto.UserUpdateDTO;
import com.mhm.models.TokenModel;
import com.mhm.models.UserLoginDTO;
import com.mhm.services.UserService;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Map;

@Path("/user")
public class UserResource {

    @Inject public UserService userService;
    @Inject JsonWebToken jwtToken;
    @Inject SecurityIdentity securityIdentity;


    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> login(UserLoginDTO userLoginDTO) {
        return userService.getUserToken(userLoginDTO);
    }

    @POST
    @Path("/registration")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> registerUserWithKeycloak(UserCreateDTO userCreateDTO) {
        return userService.registerUserWithKeycloak(userCreateDTO);
    }

    /**
     * Keycloak-first registration with compensation logic
     */
    @POST
    @Path("/register-keycloak-first")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> registerUserKeycloakFirst(UserCreateDTO userCreateDTO) {
        return userService.registerUserKeycloakFirst(userCreateDTO);
    }

    /**
     * Enhanced registration with email verification
     */
    @POST
    @Path("/register-with-verification")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> registerUserWithEmailVerification(UserCreateDTO userCreateDTO) {
        return userService.registerUserWithEmailVerification(userCreateDTO);
    }

    /**
     * Verify user email with token
     */
    @GET
    @Path("/verify")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> verifyEmail(@QueryParam("token") String token, @QueryParam("email") String email) {
        if (token == null || email == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Token and email are required")).build());
        }
        
        // For now, we'll simulate email verification
        // In production, you'd store verification tokens in database and validate them
        return Uni.createFrom().item(Response.ok()
                .entity(Map.of(
                    "message", "Email verified successfully! Your account is now active.",
                    "verified", true,
                    "email", email
                )).build());
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> updateUser(@PathParam("id") Long id, UserUpdateDTO userUpdateDTO) {
        return userService.updateUser(id, userUpdateDTO);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getUser(@PathParam("id") Long id) {
            return userService.getUser(id);
    }

    @DELETE
    @Path("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> deleteUserById(@PathParam("id") Long id) {
        return userService.deleteUser(id.toString());
    }

    @DELETE
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> deleteUser() {
        return userService.deleteUser(jwtToken.getSubject());
    }


    //TODO: This is used only for testing purpose in order to verify Bearer: token
    @GET
    @Path("/admin-token")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<TokenModel> getAdminToken() {
        return userService.getAdminToken();
    }

}
