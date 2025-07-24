package com.mhm.resources;

import com.mhm.models.UserLoginDTO;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class GeneralResource {
    @POST
    @Path("/hello")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<String> hello() {
        return Uni.createFrom().item("Hello quarkus application");
    }
}
