package algoquest.dto;

import java.util.List;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record ModuloRequisicao(String titulo, String descricao, String tema, Boolean ativo) {
    }

    public record MoverRequisicao(Integer direcao) {
    }

    public record PapelRequisicao(String papel) {
    }

    public record ImportacaoRequisicao(String modulo, List<QuestaoRequisicao> questoes, Boolean confirmar, Boolean ignorarDuplicadas) {
    }

    public record AdminModuloResposta(int id, String titulo, String descricao, String tema, boolean ativo, int ordem, int quantidade) {
    }

    public record AdminQuestaoResposta(int id, int moduloId, int ordem, String descricao, String dificuldade, int pontos,
                                       List<String> opcoes, int indiceCorreto, String explicacao, String visual) {
    }

    public record AdminUsuarioResposta(int id, String nomeUsuario, String email, String papel, int nivel, int pontuacao, String criadoEm) {
    }

    public record ErroImportacao(int indice, String mensagem) {
    }

    public record ImportacaoResposta(int total, int validas, int duplicadas, int importadas, boolean importado,
                                     List<ErroImportacao> erros, List<AdminQuestaoResposta> previa) {
    }
}
