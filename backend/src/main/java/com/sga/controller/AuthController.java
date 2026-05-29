package com.sga.controller;

import com.sga.dto.AuthResponseDto;
import com.sga.dto.LoginDto;
import com.sga.service.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    public Response login(LoginDto loginDto) {
        try {
            AuthResponseDto response = authService.login(loginDto);
            return Response.ok(response).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }
}
