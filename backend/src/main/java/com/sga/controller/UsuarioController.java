package com.sga.controller;

import com.sga.model.UsuarioModel;
import com.sga.service.UsuarioService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/v1/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioController {

    @Inject
    UsuarioService usuarioService;

    @GET
    @RolesAllowed("ADMINISTRADOR")
    public List<UsuarioModel> listar() {
        return usuarioService.listarTodos();
    }

    @POST
    @RolesAllowed("ADMINISTRADOR")
    public Response cadastrar(UsuarioModel usuario) {
        UsuarioModel novoUsuario = usuarioService.salvar(usuario);
        return Response.status(Response.Status.CREATED).entity(novoUsuario).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("ADMINISTRADOR")
    public Response buscarPorId(@PathParam("id") Integer id) {
        UsuarioModel usuario = usuarioService.buscarPorId(id);
        if (usuario == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(usuario).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMINISTRADOR")
    public Response deletar(@PathParam("id") Integer id) {
        usuarioService.deletar(id);
        return Response.noContent().build();
    }
}
