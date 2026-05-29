package com.sga.controller;

import com.sga.model.AssociadoModel;
import com.sga.service.AssociadoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/v1/associados")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssociadoController {

    @Inject
    AssociadoService associadoService;

    @GET
    @RolesAllowed({"ADMINISTRADOR", "ATENDENTE"})
    public List<AssociadoModel> listar() {
        return associadoService.listarTodos();
    }

    @POST
    @RolesAllowed({"ADMINISTRADOR", "ATENDENTE"})
    public Response cadastrar(AssociadoModel associado) {
        AssociadoModel novoAssociado = associadoService.salvar(associado);
        return Response.status(Response.Status.CREATED).entity(novoAssociado).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMINISTRADOR", "ATENDENTE"})
    public Response buscarPorId(@PathParam("id") Integer id) {
        AssociadoModel associado = associadoService.buscarPorId(id);
        if (associado == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(associado).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMINISTRADOR")
    public Response deletar(@PathParam("id") Integer id) {
        associadoService.deletar(id);
        return Response.noContent().build();
    }
}
