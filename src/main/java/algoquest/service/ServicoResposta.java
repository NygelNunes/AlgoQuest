package algoquest.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import algoquest.dto.ResultadoResposta;
import algoquest.dto.UsuarioResposta;
import algoquest.exception.ErroApi;
import algoquest.model.Exercicio;
import algoquest.model.ProgressoUsuario;
import algoquest.model.Usuario;
import algoquest.repository.ModuloRepositorio;
import algoquest.repository.ProgressoRepositorio;
import algoquest.repository.UsuarioRepositorio;

@Service
public class ServicoResposta {

    private final ModuloRepositorio modulos;
    private final ProgressoRepositorio progressos;
    private final UsuarioRepositorio usuarios;
    private final ServicoGamificacao gamificacao;
    private final TransactionTemplate transacao;

    public ServicoResposta(ModuloRepositorio modulos, ProgressoRepositorio progressos, UsuarioRepositorio usuarios,
                           ServicoGamificacao gamificacao, PlatformTransactionManager gerenteTransacao) {
        this.modulos = modulos;
        this.progressos = progressos;
        this.usuarios = usuarios;
        this.gamificacao = gamificacao;
        this.transacao = new TransactionTemplate(gerenteTransacao);
    }

    public ResultadoResposta responder(Usuario autenticado, int idExercicio, Object resposta) {
        Exercicio exercicio = modulos.buscarExercicioPorId(idExercicio);
        if (exercicio == null) {
            throw ErroApi.naoEncontrado("Exercício não encontrado.");
        }
        boolean correta;
        try {
            correta = exercicio.verificarResposta(resposta);
        } catch (IllegalArgumentException e) {
            throw ErroApi.requisicaoInvalida(e.getMessage());
        }
        if (!correta) {
            return new ResultadoResposta(false, null, 0, UsuarioResposta.de(autenticado));
        }
        return transacao.execute(status -> {
            Usuario usuario = usuarios.buscarPorIdParaAtualizar(autenticado.getId());
            if (usuario == null) {
                throw ErroApi.naoAutenticado("Sessão inválida ou expirada. Entre novamente.");
            }
            ProgressoUsuario progresso = progressos.buscarPorUsuario(usuario.getId());
            int ganhos = 0;
            if (!progresso.isConcluido(exercicio.getId())) {
                progressos.registrarConclusao(usuario.getId(), exercicio.getId());
                gamificacao.processarRespostaCorreta(usuario, exercicio);
                usuarios.salvar(usuario);
                ganhos = exercicio.getPontos();
            }
            return new ResultadoResposta(true, exercicio.getTextoFeedback(), ganhos, UsuarioResposta.de(usuario));
        });
    }
}
