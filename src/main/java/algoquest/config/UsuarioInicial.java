package algoquest.config;

import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import algoquest.model.Avatar;
import algoquest.model.Papel;
import algoquest.model.Usuario;
import algoquest.repository.UsuarioRepositorio;
import algoquest.util.HashSenha;

@Component
public class UsuarioInicial implements SmartInitializingSingleton {

    private static final Logger LOG = LoggerFactory.getLogger(UsuarioInicial.class);

    private final UsuarioRepositorio usuarios;
    private final HashSenha hashSenha;
    private final String nome;
    private final String senha;

    public UsuarioInicial(UsuarioRepositorio usuarios, HashSenha hashSenha,
                          @Value("${algoquest.usuario-inicial.nome:}") String nome,
                          @Value("${algoquest.usuario-inicial.senha:}") String senha) {
        this.usuarios = usuarios;
        this.hashSenha = hashSenha;
        this.nome = nome == null ? "" : nome.trim();
        this.senha = senha == null ? "" : senha;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (nome.isEmpty() && senha.isEmpty()) {
            return;
        }
        if (nome.isEmpty() || senha.length() < 8 || nome.length() > 50 || senha.length() > 128) {
            LOG.warn("Usuario inicial ignorado: informe nome (ate 50 caracteres) e senha (8 a 128 caracteres).");
            return;
        }
        Usuario existente = usuarios.buscarPorNome(nome);
        if (existente != null) {
            if (existente.getPapel() != Papel.MASTER && hashSenha.verificar(senha, existente.getSenha())) {
                usuarios.atualizarPapel(existente.getId(), Papel.MASTER);
                LOG.info("Usuario '{}' promovido a Master.", nome);
            } else if (existente.getPapel() != Papel.MASTER) {
                LOG.warn("Usuario '{}' ja existe e a senha informada nao confere: nenhum Master definido.", nome);
            }
            return;
        }
        Usuario novo = new Usuario(0, nome, hashSenha.gerar(senha));
        novo.setPapel(Papel.MASTER);
        novo.setAvatar(Avatar.padraoPara(nome, ThreadLocalRandom.current()));
        usuarios.salvar(novo);
        LOG.info("Usuario inicial '{}' criado como Master.", nome);
    }
}
