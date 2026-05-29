package com.sga.service;

import com.sga.model.AssociadoModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class AssociadoService {

    public List<AssociadoModel> listarTodos() {
        return AssociadoModel.listAll();
    }

    @Transactional
    public AssociadoModel salvar(AssociadoModel associado) {
        associado.persist();
        return associado;
    }

    public AssociadoModel buscarPorId(Integer id) {
        return AssociadoModel.findById(id);
    }

    @Transactional
    public void deletar(Integer id) {
        AssociadoModel associado = AssociadoModel.findById(id);
        if (associado != null) {
            associado.delete();
        }
    }
}
