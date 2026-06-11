package com.sga.service;

import com.sga.dto.AuthResponseDto;
import com.sga.dto.LoginDto;
import com.sga.model.UsuarioModel;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.jwt.Claims;
import io.quarkus.elytron.security.common.BcryptUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;

@ApplicationScoped
public class AuthService {

    public AuthResponseDto login(LoginDto loginDto) {
         UsuarioModel usuario = UsuarioModel.find("email", loginDto.email).firstResult();

        if (usuario != null && BcryptUtil.matches(loginDto.password, usuario.senhaHash)) {
            long expiresAt = Instant.now().plus(Duration.ofHours(8)).getEpochSecond();

            String token = Jwt.issuer("https://sga-api.com")
                    .upn(usuario.email)
                    .groups(new HashSet<>(Arrays.asList(usuario.perfil.name())))
                    .claim(Claims.full_name.name(), usuario.nome)
                    .expiresAt(expiresAt)
                    .sign();
            
            return new AuthResponseDto(token, usuario.nome, usuario.perfil.name());
        }
        
        throw new RuntimeException("Credenciais inválidas");
    }
}
