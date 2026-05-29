package com.sga.service;

import com.sga.model.UsuarioModel;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class UsuarioService {

    public List<UsuarioModel> listarTodos() {
        return UsuarioModel.listAll();
    }

    @Transactional
    public UsuarioModel salvar(UsuarioModel usuario) {
        if (usuario.id == null) {
            usuario.senhaHash = BcryptUtil.bcryptHash(usuario.senhaHash);
        }
        usuario.persist();
        return usuario;
    }

    public UsuarioModel buscarPorId(Integer id) {
        return UsuarioModel.findById(id);
    }

    @Transactional
    public void deletar(Integer id) {
        UsuarioModel usuario = UsuarioModel.findById(id);
        if (usuario != null) {
            usuario.delete();
        }
    }
}
