package com.sga.service;

import com.sga.model.BoletoModel;
import com.sga.model.AssociadoModel;
import com.sga.model.EmpresaModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class BoletoService {

    public List<BoletoModel> listarPorAssociado(Integer associadoId) {
        return BoletoModel.find("associado.id", associadoId).list();
    }

    @Transactional
    public BoletoModel salvar(BoletoModel boleto) {
        boleto.persist();
        return boleto;
    }

    public byte[] gerarPdf(Integer boletoId) {
        BoletoModel boleto = BoletoModel.findById(boletoId);
        if (boleto == null) {
            throw new RuntimeException("Boleto não encontrado");
        }
        
       /* EmpresaModel empresa = EmpresaModel.findFirst().firstResult();
        if (empresa == null) {
            throw new RuntimeException("Empresa não configurada");
        }*/

        // Aqui entraria a lógica do JRimum para gerar o PDF
        // Por simplicidade neste exemplo, retornamos um array vazio ou mock
        return "PDF do Boleto Mock".getBytes();
    }
}
