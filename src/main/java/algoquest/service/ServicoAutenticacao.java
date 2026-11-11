package algoquest.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import algoquest.dto.UsuarioResposta;
import algoquest.exception.ErroApi;
import algoquest.model.Usuario;
import algoquest.repository.SessaoRepositorio;
import algoquest.repository.UsuarioRepositorio;
import algoquest.util.HashSenha;

@Service
public class ServicoAutenticacao {

    static final int MAX_NOME = 50;
    static final int MAX_SENHA = 128;
    private static final String PREFIXO = "bearer ";
    private static final String MSG_CREDENCIAIS = "Usuário ou senha incorretos.";

    private final UsuarioRepositorio usuarios;
    private final SessaoRepositorio sessoes;
    private final HashSenha hashSenha;
    private final ServicoAvatar avatares;
    private final Duration validade;

    public ServicoAutenticacao(UsuarioRepositorio usuarios, SessaoRepositorio sessoes, HashSenha hashSenha,
                               ServicoAvatar avatares,
                               @Value("${algoquest.sessao.horas:12}") long horas) {
        this.usuarios = usuarios;
        this.sessoes = sessoes;
        this.hashSenha = hashSenha;
        this.avatares = avatares;
        this.validade = Duration.ofHours(horas);
    }

    public UsuarioResposta login(String nomeUsuario, String senha) {
        if (nomeUsuario == null || nomeUsuario.isBlank() || senha == null || senha.isEmpty()) {
            throw ErroApi.requisicaoInvalida("Informe usuário e senha.");
        }
        String nome = nomeUsuario.trim();
        if (nome.length() > MAX_NOME || senha.length() > MAX_SENHA) {
            throw ErroApi.naoAutenticado(MSG_CREDENCIAIS);
        }
        Usuario usuario = usuarios.buscarPorNome(nome);
        if (usuario == null) {
            hashSenha.verificarFalso(senha);
            throw ErroApi.naoAutenticado(MSG_CREDENCIAIS);
        }
        if (!hashSenha.verificar(senha, usuario.getSenha())) {
            throw ErroApi.naoAutenticado(MSG_CREDENCIAIS);
        }
        avatares.garantirAvatar(usuario);
        return abrirSessao(usuario);
    }

    private UsuarioResposta abrirSessao(Usuario usuario) {
        Instant agora = Instant.now();
        sessoes.removerExpiradas(agora);
        String token = HashSenha.gerarToken();
        sessoes.criar(HashSenha.hashDeToken(token), usuario.getId(), agora.plus(validade));
        return UsuarioResposta.comToken(avatares.comImagem(usuario), token);
    }

    public UsuarioResposta registrar(String nomeUsuario, String senha) {
        String nome = nomeUsuario == null ? "" : nomeUsuario.trim();
        if (!nome.matches("[\\p{L}\\p{N}._-]{3," + MAX_NOME + "}")) {
            throw ErroApi.requisicaoInvalida("Usuário: 3 a " + MAX_NOME + " caracteres (letras, números, ponto, hífen ou sublinhado).");
        }
        if (senha == null || senha.length() < 8 || senha.length() > MAX_SENHA) {
            throw ErroApi.requisicaoInvalida("A senha deve ter de 8 a " + MAX_SENHA + " caracteres.");
        }
        if (usuarios.buscarPorNome(nome) != null) {
            throw ErroApi.conflito("Este nome de usuário já está em uso.");
        }
        Usuario novo = new Usuario(0, nome, hashSenha.gerar(senha));
        novo.setAvatar(avatares.sortearPadrao(nome));
        try {
            usuarios.salvar(novo);
        } catch (DuplicateKeyException e) {
            throw ErroApi.conflito("Este nome de usuário já está em uso.");
        }
        return abrirSessao(novo);
    }

    public Usuario autenticar(String cabecalhoAutorizacao) {
        String token = extrairToken(cabecalhoAutorizacao);
        if (token == null) {
            throw ErroApi.naoAutenticado("Autenticação necessária.");
        }
        Integer idUsuario = sessoes.buscarIdUsuarioValido(HashSenha.hashDeToken(token), Instant.now());
        Usuario usuario = idUsuario == null ? null : usuarios.buscarPorId(idUsuario);
        if (usuario == null) {
            throw ErroApi.naoAutenticado("Sessão inválida ou expirada. Entre novamente.");
        }
        return usuario;
    }

    public void logout(String cabecalhoAutorizacao) {
        String token = extrairToken(cabecalhoAutorizacao);
        if (token != null) {
            sessoes.remover(HashSenha.hashDeToken(token));
        }
    }

    private static String extrairToken(String cabecalho) {
        if (cabecalho == null || cabecalho.length() <= PREFIXO.length()
                || !cabecalho.regionMatches(true, 0, PREFIXO, 0, PREFIXO.length())) {
            return null;
        }
        String token = cabecalho.substring(PREFIXO.length()).trim();
        return token.isEmpty() || token.length() > 200 ? null : token;
    }
}
