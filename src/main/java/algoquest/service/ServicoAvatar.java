package algoquest.service;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import algoquest.exception.ErroApi;
import algoquest.model.Avatar;
import algoquest.model.Usuario;
import algoquest.repository.UsuarioRepositorio;

@Service
public class ServicoAvatar {

    private final UsuarioRepositorio usuarios;

    public ServicoAvatar(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    public String sortearPadrao(String nomeUsuario) {
        return Avatar.padraoPara(nomeUsuario, ThreadLocalRandom.current());
    }

    public void garantirAvatar(Usuario u) {
        if (u.getAvatar() == null) {
            String padrao = sortearPadrao(u.getNomeUsuario());
            if (usuarios.definirAvatarSeVazio(u.getId(), padrao)) {
                u.setAvatar(padrao);
            } else {
                Usuario atual = usuarios.buscarPorId(u.getId());
                u.setAvatar(atual == null ? null : atual.getAvatar());
            }
        }
    }

    public Usuario escolher(Usuario u, String id) {
        if (!Avatar.idDoCatalogo(id)) {
            throw ErroApi.requisicaoInvalida("Avatar desconhecido.");
        }
        usuarios.atualizarAvatar(u.getId(), id, null);
        u.setAvatar(id);
        u.setAvatarImagem(null);
        return u;
    }

    public Usuario enviarPersonalizado(Usuario u, String dataUrl) {
        String valida;
        try {
            valida = Avatar.validarImagem(dataUrl);
        } catch (IllegalArgumentException e) {
            throw ErroApi.requisicaoInvalida(e.getMessage());
        }
        usuarios.atualizarAvatar(u.getId(), Avatar.PERSONALIZADO, valida);
        u.setAvatar(Avatar.PERSONALIZADO);
        u.setAvatarImagem(valida);
        return u;
    }

    public Usuario comImagem(Usuario u) {
        if (Avatar.PERSONALIZADO.equals(u.getAvatar())) {
            u.setAvatarImagem(usuarios.buscarAvatarImagem(u.getId()));
        }
        return u;
    }
}
