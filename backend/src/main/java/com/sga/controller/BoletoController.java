package com.sga.controller;

import com.sga.model.BoletoModel;
import com.sga.service.BoletoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/v1/boletos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BoletoController {

    @Inject
    BoletoService boletoService;

    @GET
    @Path("/associado/{associadoId}")
    @RolesAllowed({"ADMINISTRADOR", "ATENDENTE"})
    public List<BoletoModel> listarPorAssociado(@PathParam("associadoId") Integer associadoId) {
        return boletoService.listarPorAssociado(associadoId);
    }

    @POST
    @RolesAllowed({"ADMINISTRADOR", "ATENDENTE"})
    public Response criar(BoletoModel boleto) {
        BoletoModel novoBoleto = boletoService.salvar(boleto);
        return Response.status(Response.Status.CREATED).entity(novoBoleto).build();
    }

    @GET
    @Path("/{id}/pdf")
    @Produces("application/pdf")
    @RolesAllowed({"ADMINISTRADOR", "ATENDENTE"})
    public Response downloadPdf(@PathParam("id") Integer id) {
        byte[] pdf = boletoService.gerarPdf(id);
        return Response.ok(pdf)
                .header("Content-Disposition", "attachment; filename=boleto-" + id + ".pdf")
                .build();
    }
}
